package com.diaryweb.demo.repository;

import com.diaryweb.demo.entity.DiaryExchange;
import com.diaryweb.demo.entity.ExchangeStatus;
import com.diaryweb.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DiaryExchangeRepository extends JpaRepository<DiaryExchange, Long> {

    // 交换历史：我作为 requester 或 target 参与过的
    List<DiaryExchange> findByRequesterOrTarget(User requester, User target);

    // 交换历史（倒序）
    List<DiaryExchange> findByRequesterOrTargetOrderByUpdatedAtDesc(User requester, User target);

    // 防重复：同一 requester->target 的 PENDING 是否存在
    boolean existsByRequesterIdAndTargetIdAndStatus(Long requesterId, Long targetId, ExchangeStatus status);

    // 两人之间是否存在 ACCEPTED（任意方向）
    @Query("""
        select case when count(ex) > 0 then true else false end
        from DiaryExchange ex
        where ex.status = com.diaryweb.demo.entity.ExchangeStatus.ACCEPTED
          and (
                (ex.requester.id = :userAId and ex.target.id = :userBId)
             or (ex.requester.id = :userBId and ex.target.id = :userAId)
          )
    """)
    boolean existsAcceptedBetweenUsers(Long userAId, Long userBId);
}
