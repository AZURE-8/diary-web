package com.diaryweb.demo.dto;

import com.diaryweb.demo.entity.Diary;
import com.diaryweb.demo.entity.Tag; 
import com.diaryweb.demo.entity.Visibility;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;


 //日记对外展示 DTO
public class DiaryDTO {

    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private Visibility visibility;
    private UserDTO author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long likeCount;
    private Set<TagDTO> tags;

    public static class TagDTO {
        public Long id;
        public String name;

        public TagDTO(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public DiaryDTO() {
    }

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
        dto.author = UserDTO.from(diary.getUser());

        if (diary.getTags() != null) {
            dto.tags = diary.getTags().stream()
                    .map(tag -> new TagDTO(tag.getId(), tag.getName()))
                    .collect(Collectors.toSet());
        }

        return dto;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }

    public UserDTO getAuthor() { return author; }
    public void setAuthor(UserDTO author) { this.author = author; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public long getLikeCount() { return likeCount; }
    public void setLikeCount(long likeCount) { this.likeCount = likeCount; }

    public Set<TagDTO> getTags() { return tags; }
    public void setTags(Set<TagDTO> tags) { this.tags = tags; }
}