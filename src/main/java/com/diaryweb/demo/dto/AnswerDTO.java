package com.diaryweb.demo.dto;

import com.diaryweb.demo.entity.Answer;
import java.time.LocalDateTime;

public class AnswerDTO {

    private Long id;
    private String content;
    private String username;
    private LocalDateTime createdAt;

    public AnswerDTO() {
    }

    public static AnswerDTO from(Answer a) {
        AnswerDTO dto = new AnswerDTO();
        dto.id = a.getId();
        dto.content = a.getContent();
        dto.createdAt = a.getCreatedAt();

        // 匿名逻辑处理
        if (a.isAnonymous()) {
            dto.username = "匿名用户";
        } else if (a.getUser() != null) {
            dto.username = a.getUser().getUsername();
        } else {
            dto.username = "未知用户";
        }

        return dto;
    }

    // Getter
    public Long getId() { return id; }
    public String getContent() { return content; }
    public String getUsername() { return username; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setter
    public void setId(Long id) { this.id = id; }
    public void setContent(String content) { this.content = content; }
    public void setUsername(String username) { this.username = username; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}