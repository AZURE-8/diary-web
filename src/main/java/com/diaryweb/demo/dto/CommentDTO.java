package com.diaryweb.demo.dto;

import com.diaryweb.demo.entity.Comment;
import java.time.LocalDateTime;

public class CommentDTO {

    private Long id;
    private String content;
    private LocalDateTime createdAt;

    // 如果匿名，username 返回 "匿名用户"
    private String username;

    public static CommentDTO from(Comment c) {
        CommentDTO dto = new CommentDTO();
        dto.id = c.getId();
        dto.content = c.getContent();
        dto.createdAt = c.getCreatedAt();

        if (c.isAnonymous()) {
            dto.username = "匿名用户";
        } else {
            dto.username = c.getUser().getUsername();
        }
        return dto;
    }

    // getters
    public Long getId() { return id; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getUsername() { return username; }
}
