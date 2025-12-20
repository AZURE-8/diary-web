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

    private User currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new BizException(4010, "未登录或认证信息缺失");
        }

        String username = auth.getName();
        User u = userRepository.findByUsername(username);
        if (u == null) {
            throw new BizException(4010, "用户不存在或已被删除");
        }
        return u;
    }

    // =========================
    // 1) 日记详情
    // =========================
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
    // 2) 创建日记
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

        diary.setTags(buildTags(tagNames));

        Diary savedDiary = diaryRepository.save(diary);
        experienceService.award(me.getId(), 3, "CREATE_DIARY");

        return savedDiary;
    }

    // =========================
    // 3) 查看“我的”所有日记
    // =========================
    public List<Diary> listMyDiaries() {
        User me = currentUser();
        return diaryRepository.findByUserId(me.getId());
    }

    // =========================
    // 4) 查看指定用户的公开日记
    // =========================
    public List<Diary> listUserPublicDiaries(Long userId) {
        if (userId == null) {
            throw BizException.badRequest("userId 不能为空");
        }
        return diaryRepository.findByUserIdAndVisibility(userId, Visibility.PUBLIC);
    }

    // =========================
    // 5) 更新日记
    // =========================
    public Diary updateDiary(Long diaryId, String title, String content,
                             Visibility visibility, Set<String> tagNames) {

        User me = currentUser();

        if (diaryId == null) {
            throw BizException.badRequest("diaryId 不能为空");
        }

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> BizException.notFound("日记不存在"));

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

        if (tagNames != null) {
            diary.setTags(buildTags(tagNames));
        }

        diary.setUpdatedAt(LocalDateTime.now());
        return diaryRepository.save(diary);
    }

    // =========================
    // 6) 删除日记
    // =========================
    public void deleteDiary(Long diaryId) {

        User me = currentUser();

        if (diaryId == null) {
            throw BizException.badRequest("diaryId 不能为空");
        }

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> BizException.notFound("日记不存在"));

        if (diary.getUser() == null || diary.getUser().getId() == null
                || !diary.getUser().getId().equals(me.getId())) {
            throw BizException.forbidden("无权限删除他人日记");
        }

        diaryRepository.delete(diary);
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

 // 在 DiaryService.java 中找到原有的 pageUserPublicDiaries 方法，替换为：

    // =========================
    // 9) 分页：某用户公开日记 (支持搜索)
    // =========================
    public Page<Diary> pageUserPublicDiaries(Long userId, String keyword, Pageable pageable) {
        if (userId == null) {
            throw BizException.badRequest("userId 不能为空");
        }
        if (pageable == null) {
            throw BizException.badRequest("pageable 不能为空");
        }
        
        // 如果没有关键词，走原有的查询（性能稍好）
        if (keyword == null || keyword.trim().isEmpty()) {
            return diaryRepository.findByUserIdAndVisibility(userId, Visibility.PUBLIC, pageable);
        } else {
            // 如果有关键词，走新增的搜索查询
            return diaryRepository.searchUserPublicDiaries(userId, Visibility.PUBLIC, keyword.trim(), pageable);
        }
    }


    // =========================
    // 11) 分页：全能搜索（标题/内容/作者/标签，公开 + 半私密）
    // =========================
    public Page<Diary> pagePublicByKeyword(String keyword, Pageable pageable) {
        if (pageable == null) {
            throw BizException.badRequest("pageable 不能为空");
        }

        String kw = (keyword == null) ? "" : keyword.trim();

        // 调用 searchDiaries 并传入 PUBLIC 和 SEMI_PRIVATE
        return diaryRepository.searchDiaries(
                List.of(Visibility.PUBLIC, Visibility.SEMI_PRIVATE), 
                kw,
                pageable
        );
    }

    // =========================
    // 工具方法：标签集合构建
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