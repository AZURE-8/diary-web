package com.diaryweb.demo.service;

import com.diaryweb.demo.entity.*;
import com.diaryweb.demo.repository.DiaryExchangeRepository;
import com.diaryweb.demo.repository.DiaryRepository;
import com.diaryweb.demo.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExchangeService {

    private final DiaryExchangeRepository exchangeRepository;
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;

    public ExchangeService(DiaryExchangeRepository exchangeRepository,
                           DiaryRepository diaryRepository,
                           UserRepository userRepository) {
        this.exchangeRepository = exchangeRepository;
        this.diaryRepository = diaryRepository;
        this.userRepository = userRepository;
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userRepository.findByUsername(username);
        if (u == null) throw new RuntimeException("当前用户不存在");
        return u;
    }

    // 发起交换：我( requester ) -> 对方(target) ，我提供 requesterDiary
    public DiaryExchange requestExchange(Long targetUserId, Long requesterDiaryId) {
        User me = currentUser();
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("目标用户不存在"));

        Diary myDiary = diaryRepository.findById(requesterDiaryId)
                .orElseThrow(() -> new RuntimeException("日记不存在"));

        // 只能用自己的日记
        if (!myDiary.getUser().getId().equals(me.getId())) {
            throw new RuntimeException("不能拿别人的日记发起交换");
        }

        // 只能交换半私密日记
        if (myDiary.getVisibility() != Visibility.SEMI_PRIVATE) {
            throw new RuntimeException("只能用 SEMI_PRIVATE 日记发起交换");
        }

        DiaryExchange ex = new DiaryExchange();
        ex.setRequester(me);
        ex.setTarget(target);
        ex.setRequesterDiary(myDiary);
        ex.setStatus(ExchangeStatus.PENDING);
        ex.setCreatedAt(LocalDateTime.now());
        ex.setUpdatedAt(LocalDateTime.now());

        return exchangeRepository.save(ex);
    }

    // 接收方同意交换：提供 targetDiary
    public DiaryExchange acceptExchange(Long exchangeId, Long targetDiaryId) {
        User me = currentUser();
        DiaryExchange ex = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new RuntimeException("交换记录不存在"));

        // 必须是接收方才能操作
        if (!ex.getTarget().getId().equals(me.getId())) {
            throw new RuntimeException("无权限操作该交换");
        }

        if (ex.getStatus() != ExchangeStatus.PENDING) {
            throw new RuntimeException("该交换已处理，不能重复操作");
        }

        Diary myDiary = diaryRepository.findById(targetDiaryId)
                .orElseThrow(() -> new RuntimeException("回赠日记不存在"));

        // 只能用自己的日记
        if (!myDiary.getUser().getId().equals(me.getId())) {
            throw new RuntimeException("不能用别人的日记作为回赠");
        }

        if (myDiary.getVisibility() != Visibility.SEMI_PRIVATE) {
            throw new RuntimeException("回赠日记必须是 SEMI_PRIVATE");
        }

        ex.setTargetDiary(myDiary);
        ex.setStatus(ExchangeStatus.ACCEPTED);
        ex.setUpdatedAt(LocalDateTime.now());

        return exchangeRepository.save(ex);
    }

    // 接收方拒绝交换
    public DiaryExchange rejectExchange(Long exchangeId) {
        User me = currentUser();
        DiaryExchange ex = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new RuntimeException("交换记录不存在"));

        if (!ex.getTarget().getId().equals(me.getId())) {
            throw new RuntimeException("无权限操作该交换");
        }

        if (ex.getStatus() != ExchangeStatus.PENDING) {
            throw new RuntimeException("该交换已处理，不能重复操作");
        }

        ex.setStatus(ExchangeStatus.REJECTED);
        ex.setUpdatedAt(LocalDateTime.now());

        return exchangeRepository.save(ex);
    }

    // 交换历史：我参与过的全部
    public List<DiaryExchange> history() {
        User me = currentUser();
        return exchangeRepository.findByRequesterOrTarget(me, me);
    }
}
