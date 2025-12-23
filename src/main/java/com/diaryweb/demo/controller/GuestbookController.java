package com.diaryweb.demo.controller;

import com.diaryweb.demo.common.ApiResponse;
import com.diaryweb.demo.dto.GuestbookMessageDTO;
import com.diaryweb.demo.entity.GuestbookMessage;
import com.diaryweb.demo.entity.User;
import com.diaryweb.demo.repository.GuestbookMessageRepository;
import com.diaryweb.demo.repository.UserRepository;
import com.diaryweb.demo.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/guestbook")
public class GuestbookController {

    private final GuestbookMessageRepository guestbookRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    public GuestbookController(GuestbookMessageRepository guestbookRepository,
                               UserService userService,
                               UserRepository userRepository) {
        this.guestbookRepository = guestbookRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public static class MessageRequest {
        public String content;
    }

    // 获取某个用户的留言板
    @GetMapping("/{userId}")
    public ApiResponse<List<GuestbookMessageDTO>> listMessages(@PathVariable Long userId) {
        // 获取当前登录用户ID，用于判断删除权限（未登录则为null）
        Long currentUserId = null;
        try { currentUserId = userService.currentUser().getId(); } catch (Exception e) {}

        List<GuestbookMessage> list = guestbookRepository.findByReceiverIdOrderByCreatedAtDesc(userId);
        
        Long finalCurrentUserId = currentUserId;
        List<GuestbookMessageDTO> dtos = list.stream()
                .map(msg -> GuestbookMessageDTO.from(msg, finalCurrentUserId))
                .toList();
        
        return ApiResponse.ok(dtos);
    }

    // 发布留言
    @PostMapping("/{userId}")
    public ApiResponse<GuestbookMessageDTO> postMessage(@PathVariable Long userId, @RequestBody MessageRequest req) {
        User me = userService.currentUser();
        User receiver = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("目标用户不存在"));

        if (req.content == null || req.content.trim().isEmpty()) {
            return ApiResponse.fail(400, "留言内容不能为空");
        }

        GuestbookMessage msg = new GuestbookMessage();
        msg.setSender(me);
        msg.setReceiver(receiver);
        msg.setContent(req.content.trim());
        msg.setCreatedAt(LocalDateTime.now());

        GuestbookMessage saved = guestbookRepository.save(msg);
        return ApiResponse.ok(GuestbookMessageDTO.from(saved, me.getId()));
    }

    // 删除留言
    @DeleteMapping("/{messageId}")
    public ApiResponse<String> deleteMessage(@PathVariable Long messageId) {
        User me = userService.currentUser();
        GuestbookMessage msg = guestbookRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("留言不存在"));

        boolean isSender = msg.getSender().getId().equals(me.getId());
        boolean isOwner = msg.getReceiver().getId().equals(me.getId());

        if (!isSender && !isOwner) {
            return ApiResponse.fail(403, "无权删除该留言");
        }

        guestbookRepository.delete(msg);
        return ApiResponse.ok("deleted");
    }
}