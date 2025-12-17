package com.diaryweb.demo.service;

import com.diaryweb.demo.common.BizException;
import com.diaryweb.demo.entity.*;
import com.diaryweb.demo.repository.DiaryRepository;
import com.diaryweb.demo.repository.TagRepository;
import com.diaryweb.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final ExperienceService experienceService;

    // ✅ 第四天新增：用于 SEMI_PRIVATE 可见性校验
    private final ExchangeService exchangeService;

    public DiaryService(DiaryRepository diaryRepository,
                        UserRepository userRepository,
                        TagRepository tagRepository,
                        ExperienceService experienceService,
                        ExchangeService exchangeService) {
        this.diaryRepository = diaryRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.experienceService = experienceService;
        this.exchangeService = exchangeService;
    }
    
    public Long currentUserId() {
        return currentUser().getId();
    }

    // 获取当前登录用户（JWT/Basic Auth 都适用：Authentication.getName() 是 username）
    private User currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            // 更合理：未登录
            throw new BizException(4010, "未登录或认证信息缺失");
        }

        String username = auth.getName();
        User u = userRepository.findByUsername(username);
        if (u == null) {
            // 更合理：认证用户不存在
            throw new BizException(4010, "用户不存在或已被删除");
        }
        return u;
    }

    // =========================
    // 1) 日记详情
    // =========================
    /**
     * 获取日记详情（统一权限入口）
     * 规则：
     * - 作者本人：任意可见性都可查看
     * - 非作者：
     *   - PUBLIC：可查看
     *   - PRIVATE：禁止
     *   - SEMI_PRIVATE：仅在与作者交换成功（ACCEPTED）后可查看
     */
    public Diary getDiaryDetail(Long diaryId) {
        if (diaryId == null) {
            throw BizException.badRequest("diaryId 不能为空");
        }

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> BizException.notFound("日记不存在"));

        User me = currentUser();

        // 作者本人：直接允许
        if (diary.getUser() != null && diary.getUser().getId() != null
                && diary.getUser().getId().equals(me.getId())) {
            return diary;
        }

        Visibility v = diary.getVisibility() == null ? Visibility.PRIVATE : diary.getVisibility();

        // PUBLIC：允许
        if (v == Visibility.PUBLIC) {
            return diary;
        }

        // PRIVATE：禁止
        if (v == Visibility.PRIVATE) {
            throw BizException.forbidden("该日记为私密，无法访问");
        }

        // SEMI_PRIVATE：交换成功才允许
        Long authorId = diary.getUser() == null ? null : diary.getUser().getId();
        if (authorId == null) {
            throw BizException.badRequest("日记作者信息异常");
        }

        boolean allowed = exchangeService.hasAcceptedExchangeBetween(me.getId(), authorId);
        if (!allowed) {
            throw BizException.forbidden("该日记为半私密，需交换成功后查看");
        }

        return diary;
    }

    // =========================
    // 2) 创建日记（支持标签 + 可见性）
    // =========================
    public Diary createDiary(String title, String content, String imageUrl,
                             Visibility visibility, Set<String> tagNames) {

        User me = currentUser();

        if (title == null || title.isBlank()) {
            throw BizException.badRequest("标题不能为空");
        }

        Diary diary = new Diary();
        diary.setUser(me);
        diary.setTitle(title.trim());
        diary.setContent(content);
        diary.setImageUrl(imageUrl);
        diary.setVisibility(visibility == null ? Visibility.PRIVATE : visibility);
        diary.setCreatedAt(LocalDateTime.now());
        diary.setUpdatedAt(LocalDateTime.now());

        // 标签：不存在就创建，存在就复用
        diary.setTags(buildTags(tagNames));

        // ① 保存日记
        Diary savedDiary = diaryRepository.save(diary);

        // ② 保存成功后加经验
        experienceService.award(me.getId(), 3, "CREATE_DIARY");

        return savedDiary;
    }

    // =========================
    // 3) 查看“我的”所有日记（包括 PRIVATE）
    // =========================
    public List<Diary> listMyDiaries() {
        User me = currentUser();
        return diaryRepository.findByUserId(me.getId());
    }

    // =========================
    // 4) 查看指定用户的公开日记（别人只能看 PUBLIC）
    // =========================
    public List<Diary> listUserPublicDiaries(Long userId) {
        if (userId == null) {
            throw BizException.badRequest("userId 不能为空");
        }
        return diaryRepository.findByUserIdAndVisibility(userId, Visibility.PUBLIC);
    }

    // =========================
    // 5) 更新日记（仅作者）
    // =========================
    public Diary updateDiary(Long diaryId, String title, String content,
                             Visibility visibility, Set<String> tagNames) {

        User me = currentUser();

        if (diaryId == null) {
            throw BizException.badRequest("diaryId 不能为空");
        }

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> BizException.notFound("日记不存在"));

        // 强制作者校验
        if (diary.getUser() == null || diary.getUser().getId() == null
                || !diary.getUser().getId().equals(me.getId())) {
            throw BizException.forbidden("无权限修改他人日记");
        }

        if (title != null) {
            if (title.isBlank()) {
                throw BizException.badRequest("标题不能为空");
            }
            diary.setTitle(title.trim());
        }

        if (content != null) diary.setContent(content);
        if (visibility != null) diary.setVisibility(visibility);

        // 更新标签（如果传了 tagNames，就用新的标签集合覆盖旧的）
        if (tagNames != null) {
            diary.setTags(buildTags(tagNames));
        }

        diary.setUpdatedAt(LocalDateTime.now());
        return diaryRepository.save(diary);
    }

    // =========================
    // 6) 删除日记（仅作者）
    // =========================
    public void deleteDiary(Long diaryId) {

        User me = currentUser();

        if (diaryId == null) {
            throw BizException.badRequest("diaryId 不能为空");
        }

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> BizException.notFound("日记不存在"));

        // 强制作者校验
        if (diary.getUser() == null || diary.getUser().getId() == null
                || !diary.getUser().getId().equals(me.getId())) {
            throw BizException.forbidden("无权限删除他人日记");
        }

        diaryRepository.delete(diary);
    }

    // =========================
    // 7) 按标签搜索公开日记（发现页）
    // =========================
    public List<Diary> searchPublicByTag(String tagName) {
        if (tagName == null || tagName.isBlank()) {
            throw BizException.badRequest("tag 不能为空");
        }
        return diaryRepository.findByVisibilityAndTags_Name(Visibility.PUBLIC, tagName.trim());
    }

    // =========================
    // 8) 分页：我的日记
    // =========================
    public Page<Diary> pageMyDiaries(Pageable pageable) {
        if (pageable == null) {
            throw BizException.badRequest("pageable 不能为空");
        }
        User me = currentUser();
        return diaryRepository.findByUserId(me.getId(), pageable);
    }

    // =========================
    // 9) 分页：某用户公开日记
    // =========================
    public Page<Diary> pageUserPublicDiaries(Long userId, Pageable pageable) {
        if (userId == null) {
            throw BizException.badRequest("userId 不能为空");
        }
        if (pageable == null) {
            throw BizException.badRequest("pageable 不能为空");
        }
        return diaryRepository.findByUserIdAndVisibility(userId, Visibility.PUBLIC, pageable);
    }

    // =========================
    // 10) 分页：公开日记按标签
    // =========================
    public Page<Diary> pagePublicByTag(String tag, Pageable pageable) {
        if (tag == null || tag.isBlank()) {
            throw BizException.badRequest("tag 不能为空");
        }
        if (pageable == null) {
            throw BizException.badRequest("pageable 不能为空");
        }
        return diaryRepository.findByVisibilityAndTags_Name(Visibility.PUBLIC, tag.trim(), pageable);
    }

    // =========================
    // 11) 分页：公开日记关键字搜索
    // =========================
    public Page<Diary> pagePublicByKeyword(String keyword, Pageable pageable) {
        /*if (keyword == null || keyword.isBlank()) {
            throw BizException.badRequest("keyword 不能为空");
        }*/
        if (pageable == null) {
            throw BizException.badRequest("pageable 不能为空");
        }

        String kw = keyword.trim();

        return diaryRepository.findByVisibilityInAndTitleContainingIgnoreCaseOrVisibilityInAndContentContainingIgnoreCase(
        		List.of(Visibility.PUBLIC, Visibility.SEMI_PRIVATE), 
                kw,
                List.of(Visibility.PUBLIC, Visibility.SEMI_PRIVATE), 
                kw,
                pageable
        );
    }

    // =========================
    // 工具方法：标签集合构建（复用）
    // =========================
    private Set<Tag> buildTags(Set<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        if (tagNames == null) return tags;

        for (String name : tagNames) {
            if (name == null) continue;
            String normalized = name.trim();
            if (normalized.isEmpty()) continue;

            Tag t = tagRepository.findByName(normalized);
            if (t == null) {
                t = tagRepository.save(new Tag(normalized));
            }
            tags.add(t);
        }
        return tags;
    }
}
