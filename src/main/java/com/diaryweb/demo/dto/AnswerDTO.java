package com.diaryweb.demo.dto;

import com.diaryweb.demo.entity.Answer;
import java.time.LocalDateTime;

public class AnswerDTO {

    private Long id;
    private String content;
    private String username;
    private LocalDateTime createdAt;

    public static AnswerDTO from(Answer a) {
        AnswerDTO dto = new AnswerDTO();
        dto.id = a.getId();
        dto.content = a.getContent();
        dto.createdAt = a.getCreatedAt();

        dto.username = a.isAnonymous()
                ? "匿名用户"
                : a.getUser().getUsername();

        return dto;
    }

    // getters
}
