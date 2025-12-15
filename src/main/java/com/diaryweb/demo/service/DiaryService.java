package com.diaryweb.demo.service;

import com.diaryweb.demo.common.BizException;
import com.diaryweb.demo.entity.*;
import com.diaryweb.demo.repository.DiaryRepository;
import com.diaryweb.demo.repository.TagRepository;
import com.diaryweb.demo.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    public DiaryService(DiaryRepository diaryRepository,
                        UserRepository userRepository,
                        TagRepository tagRepository,
                        ExperienceService experienceService) {
        this.diaryRepository = diaryRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.experienceService = experienceService;
    }

    // 获取当前登录用户（JWT/Basic Auth 都适用：Authentication.getName() 是 username）
    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userRepository.findByUsername(username);
        if (u == null) {
            throw BizException.badRequest("当前用户不存在");
        }
        return u;
    }

    // 创建日记（支持标签 + 可见性）
    public Diary createDiary(String title, String content, String imageUrl,
                             Visibility visibility, Set<String> tagNames) {

        User me = currentUser();

        if (title == null || title.isBlank()) {
            throw BizException.badRequest("标题不能为空");
        }

        Diary diary = new Diary();
        diary.setUser(me);
        diary.setTitle(title);
        diary.setContent(content);
        diary.setImageUrl(imageUrl);
        diary.setVisibility(visibility == null ? Visibility.PRIVATE : visibility);
        diary.setCreatedAt(LocalDateTime.now());
        diary.setUpdatedAt(LocalDateTime.now());

        // 标签：不存在就创建，存在就复用
        Set<Tag> tags = new HashSet<>();
        if (tagNames != null) {
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
        }
        // ⭐ 关键：把 tags 放回 diary（你原代码漏了这句）
        diary.setTags(tags);

        // ① 保存日记
        Diary savedDiary = diaryRepository.save(diary);

        // ② 保存成功后加经验
        experienceService.award(me.getId(), 3, "CREATE_DIARY");

        return savedDiary;
    }

    // 查看“我的”所有日记（包括 private）
    public List<Diary> listMyDiaries() {
        User me = currentUser();
        return diaryRepository.findByUserId(me.getId());
    }

    // 查看指定用户的公开日记（别人只能看 PUBLIC）
    public List<Diary> listUserPublicDiaries(Long userId) {
        if (userId == null) {
            throw BizException.badRequest("userId 不能为空");
        }
        return diaryRepository.findByUserIdAndVisibility(userId, Visibility.PUBLIC);
    }

    // 更新日记（仅允许作者修改）
    public Diary updateDiary(Long diaryId, String title, String content,
                             Visibility visibility, Set<String> tagNames) {

        User me = currentUser();

        if (diaryId == null) {
            throw BizException.badRequest("diaryId 不能为空");
        }

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> BizException.notFound("日记不存在"));

        if (!diary.getUser().getId().equals(me.getId())) {
            throw BizException.forbidden("无权限修改他人日记");
        }

        if (title != null) {
            if (title.isBlank()) {
                throw BizException.badRequest("标题不能为空");
            }
            diary.setTitle(title);
        }

        if (content != null) diary.setContent(content);
        if (visibility != null) diary.setVisibility(visibility);

        // 更新标签（如果传了 tagNames，就用新的标签集合覆盖旧的）
        if (tagNames != null) {
            Set<Tag> tags = new HashSet<>();
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
            diary.setTags(tags);
        }

        diary.setUpdatedAt(LocalDateTime.now());
        return diaryRepository.save(diary);
    }

    // 删除日记（仅允许作者删除）
    public void deleteDiary(Long diaryId) {

        User me = currentUser();

        if (diaryId == null) {
            throw BizException.badRequest("diaryId 不能为空");
        }

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> BizException.notFound("日记不存在"));

        if (!diary.getUser().getId().equals(me.getId())) {
            throw BizException.forbidden("无权限删除他人日记");
        }

        diaryRepository.delete(diary);
    }

    // 按标签搜索公开日记（便于做广场/发现页）
    public List<Diary> searchPublicByTag(String tagName) {
        if (tagName == null || tagName.isBlank()) {
            throw BizException.badRequest("tag 不能为空");
        }
        return diaryRepository.findByVisibilityAndTags_Name(Visibility.PUBLIC, tagName.trim());
    }

    public Page<Diary> pageMyDiaries(Pageable pageable) {
        User me = currentUser();
        return diaryRepository.findByUserId(me.getId(), pageable);
    }

    public Page<Diary> pageUserPublicDiaries(Long userId, Pageable pageable) {
        return diaryRepository.findByUserIdAndVisibility(userId, Visibility.PUBLIC, pageable);
    }

    public Page<Diary> pagePublicByTag(String tag, Pageable pageable) {
        return diaryRepository.findByVisibilityAndTags_Name(Visibility.PUBLIC, tag, pageable);
    }

    public Page<Diary> pagePublicByKeyword(String keyword, Pageable pageable) {
        return diaryRepository.findByVisibilityAndTitleContainingIgnoreCaseOrVisibilityAndContentContainingIgnoreCase(
                Visibility.PUBLIC, keyword,
                Visibility.PUBLIC, keyword,
                pageable
        );
    }  
}
