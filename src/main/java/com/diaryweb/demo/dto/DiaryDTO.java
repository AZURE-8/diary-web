package com.diaryweb.demo.dto;

import com.diaryweb.demo.entity.Diary;
import com.diaryweb.demo.entity.Visibility;

import java.time.LocalDateTime;

/**
 * 日记对外展示 DTO
 * 解决 User <-> Diary 循环引用问题
 */
public class DiaryDTO {

    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private Visibility visibility;

    // 作者信息（DTO，而不是 User Entity）
    private UserDTO author;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 扩展字段（非 Diary 表字段）
    private long likeCount;

    public DiaryDTO() {
    }

    /**
     * Entity -> DTO 转换
     *
     * @param diary     Diary 实体
     * @param likeCount 当前日记点赞数（来自 likes 表）
     */
    public static DiaryDTO from(Diary diary, long likeCount) {
        if (diary == null) {
            return null;
        }

        DiaryDTO dto = new DiaryDTO();
        dto.id = diary.getId();
        dto.title = diary.getTitle();
        dto.content = diary.getContent();
        dto.imageUrl = diary.getImageUrl();
        dto.visibility = diary.getVisibility();
        dto.createdAt = diary.getCreatedAt();
        dto.updatedAt = diary.getUpdatedAt();
        dto.likeCount = likeCount;

        // ⭐ 关键：作者只用 UserDTO，避免 JSON 循环
        dto.author = UserDTO.from(diary.getUser());

        return dto;
    }

    /* getters & setters */

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public UserDTO getAuthor() {
        return author;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public void setAuthor(UserDTO author) {
        this.author = author;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }
}
