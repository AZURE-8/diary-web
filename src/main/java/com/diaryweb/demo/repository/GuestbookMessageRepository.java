package com.diaryweb.demo.repository;

import com.diaryweb.demo.entity.GuestbookMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GuestbookMessageRepository extends JpaRepository<GuestbookMessage, Long> {
    // 查询写给某个用户的留言，按时间倒序排列
    List<GuestbookMessage> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);
}