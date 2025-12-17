// src/main/java/com/diaryweb/demo/dto/QuestionDTO.java
package com.diaryweb.demo.dto;

import com.diaryweb.demo.entity.Question;
import java.time.LocalDateTime;

public class QuestionDTO {
    private Long id;
    private String content;
    private String username;
    private Long authorId; // 新增：用于判断是否是本人
    private LocalDateTime createdAt;
    private int answerCount; // 新增：用于存储回答数量

    public static QuestionDTO from(Question q) {
        QuestionDTO dto = new QuestionDTO();
        dto.id = q.getId();
        dto.content = q.getContent();
        dto.createdAt = q.getCreatedAt();
        dto.username = q.isAnonymous() ? "匿名用户" : q.getUser().getUsername();
        dto.authorId = q.getUser() != null ? q.getUser().getId() : null;

        // 核心：统计回答数
        if (q.getAnswers() != null) {
            dto.answerCount = q.getAnswers().size();
        } else {
            dto.answerCount = 0;
        }
        return dto;
    }

    // Getters (必须提供，Jackson 序列化需要)
    public Long getId() { return id; }
    public String getContent() { return content; }
    public String getUsername() { return username; }
    public Long getAuthorId() { return authorId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getAnswerCount() { return answerCount; }
}