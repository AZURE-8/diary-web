package com.diaryweb.demo.dto;

import com.diaryweb.demo.entity.GuestbookMessage;
import java.time.LocalDateTime;

public class GuestbookMessageDTO {
    public Long id;
    public String content;
    public LocalDateTime createdAt;
    public Long senderId;
    public String senderName;
    public String senderAvatarUrl;
    public boolean canDelete; // 是否有权删除

    public static GuestbookMessageDTO from(GuestbookMessage msg, Long currentUserId) {
        GuestbookMessageDTO dto = new GuestbookMessageDTO();
        dto.id = msg.getId();
        dto.content = msg.getContent();
        dto.createdAt = msg.getCreatedAt();
        if (msg.getSender() != null) {
            dto.senderId = msg.getSender().getId();
            dto.senderName = msg.getSender().getUsername();
            dto.senderAvatarUrl = msg.getSender().getAvatarUrl();
        }
        // 允许删除的条件：我是留言的发送者 OR 我是主页的主人
        if (currentUserId != null) {
            boolean isSender = msg.getSender().getId().equals(currentUserId);
            boolean isOwner = msg.getReceiver().getId().equals(currentUserId);
            dto.canDelete = isSender || isOwner;
        }
        return dto;
    }
}