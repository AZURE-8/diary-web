package com.diaryweb.demo.dto;

import com.diaryweb.demo.entity.Question;
import java.time.LocalDateTime;

public class QuestionDTO {
    private Long id;
    private String content;
    private String username;
    private Long authorId; 
    private LocalDateTime createdAt;
    private int answerCount; 

    public static QuestionDTO from(Question q) {
        QuestionDTO dto = new QuestionDTO();
        dto.id = q.getId();
        dto.content = q.getContent();
        dto.createdAt = q.getCreatedAt();
        dto.username = q.isAnonymous() ? "匿名用户" : q.getUser().getUsername();
        dto.authorId = q.getUser() != null ? q.getUser().getId() : null;

        // 统计回答数
        if (q.getAnswers() != null) {
            dto.answerCount = q.getAnswers().size();
        } else {
            dto.answerCount = 0;
        }
        return dto;
    }

    // getters
    public Long getId() { return id; }
    public String getContent() { return content; }
    public String getUsername() { return username; }
    public Long getAuthorId() { return authorId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getAnswerCount() { return answerCount; }
}