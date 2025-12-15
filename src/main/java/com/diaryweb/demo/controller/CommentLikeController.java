package com.diaryweb.demo.controller;

import com.diaryweb.demo.common.ApiResponse;
import com.diaryweb.demo.entity.Comment;
import com.diaryweb.demo.service.CommentLikeService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/diaries")
public class CommentLikeController {

    private final CommentLikeService service;

    public CommentLikeController(CommentLikeService service) {
        this.service = service;
    }

    public static class CommentRequest {
        public String content;
    }

    // 轻量 CommentDTO：避免返回 Comment(Entity)->user->...
    public static class CommentDTO {
        public Long id;
        public String content;
        public String username;
        public LocalDateTime createdAt;

        public static CommentDTO from(Comment c) {
            CommentDTO dto = new CommentDTO();
            dto.id = c.getId();
            dto.content = c.getContent();
            dto.createdAt = c.getCreatedAt();
            dto.username = (c.getUser() == null) ? null : c.getUser().getUsername();
            return dto;
        }
    }

    @PostMapping("/{diaryId}/comment")
    public ApiResponse<CommentDTO> comment(@PathVariable Long diaryId, @RequestBody CommentRequest req) {
        Comment c = service.addComment(diaryId, req.content);
        return ApiResponse.ok(CommentDTO.from(c));
    }

    @GetMapping("/{diaryId}/comments")
    public ApiResponse<List<CommentDTO>> listComments(@PathVariable Long diaryId) {
        List<Comment> list = service.listComments(diaryId);
        return ApiResponse.ok(list.stream().map(CommentDTO::from).toList());
    }

    @PostMapping("/{diaryId}/like")
    public ApiResponse<Long> like(@PathVariable Long diaryId) {
        long count = service.like(diaryId);
        return ApiResponse.ok(count);
    }

    @PostMapping("/{diaryId}/unlike")
    public ApiResponse<Long> unlike(@PathVariable Long diaryId) {
        long count = service.unlike(diaryId);
        return ApiResponse.ok(count);
    }

    @GetMapping("/{diaryId}/likes/count")
    public ApiResponse<Long> likeCount(@PathVariable Long diaryId) {
        return ApiResponse.ok(service.likeCount(diaryId));
    }
}
