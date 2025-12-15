package com.diaryweb.demo.dto;

import com.diaryweb.demo.entity.Question;
import java.time.LocalDateTime;

public class QuestionDTO {

    private Long id;
    private String content;
    private String username;
    private LocalDateTime createdAt;

    public static QuestionDTO from(Question q) {
        QuestionDTO dto = new QuestionDTO();
        dto.id = q.getId();
        dto.content = q.getContent();
        dto.createdAt = q.getCreatedAt();

        dto.username = q.isAnonymous()
                ? "匿名用户"
                : q.getUser().getUsername();

        return dto;
    }

    // getters
    public Long getId() { return id; }
    public String getContent() { return content; }
    public String getUsername() { return username; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
