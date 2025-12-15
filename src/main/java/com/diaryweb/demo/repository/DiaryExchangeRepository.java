package com.diaryweb.demo.repository;

import com.diaryweb.demo.entity.DiaryExchange;
import com.diaryweb.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiaryExchangeRepository extends JpaRepository<DiaryExchange, Long> {

    // 我发起的交换
    List<DiaryExchange> findByRequester(User requester);

    // 我收到的交换
    List<DiaryExchange> findByTarget(User target);

    // 历史：我参与的所有交换
    List<DiaryExchange> findByRequesterOrTarget(User requester, User target);
}
