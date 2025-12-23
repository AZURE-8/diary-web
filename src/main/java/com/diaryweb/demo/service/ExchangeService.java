package com.diaryweb.demo.service;

import com.diaryweb.demo.common.BizException;
import com.diaryweb.demo.entity.*;
import com.diaryweb.demo.repository.DiaryExchangeRepository;
import com.diaryweb.demo.repository.DiaryRepository;
import com.diaryweb.demo.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

//日记交换业务逻辑服务层:处理发起交换、接受/拒绝交换以及查询交换历史等功能
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
        if (username == null || username.isBlank() || "anonymousUser".equals(username)) {
            throw new BizException(4010, "未登录或 token 无效");
        }
        User u = userRepository.findByUsername(username);
        if (u == null) throw new BizException(4010, "当前用户不存在");
        return u;
        
        
    }

    public boolean hasAcceptedExchangeBetween(Long userAId, Long userBId) {
        if (userAId == null || userBId == null) return false;
        if (userAId.equals(userBId)) return true;

        return exchangeRepository.existsAcceptedBetweenUsers(userAId, userBId);
    }

    //发起交换请求
    public DiaryExchange requestExchange(Long targetUserId, Long requesterDiaryId) {
        User me = currentUser();

        if (targetUserId == null) {
            throw BizException.badRequest("targetUserId 不能为空");
        }
        if (requesterDiaryId == null) {
            throw BizException.badRequest("requesterDiaryId 不能为空");
        }
        if (me.getId().equals(targetUserId)) {
            throw BizException.badRequest("不能向自己发起交换");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> BizException.notFound("目标用户不存在"));

        Diary myDiary = diaryRepository.findById(requesterDiaryId)
                .orElseThrow(() -> BizException.notFound("日记不存在"));

        // 只能用自己的日记
        if (myDiary.getUser() == null || !myDiary.getUser().getId().equals(me.getId())) {
            throw BizException.forbidden("不能拿别人的日记发起交换");
        }

        // 只能交换半私密日记
        if (myDiary.getVisibility() != Visibility.SEMI_PRIVATE) {
            throw BizException.badRequest("只能用 SEMI_PRIVATE 日记发起交换");
        }

        // 防重复：同一交换双方已有待处理交换，就不允许再发
        boolean alreadyPending = exchangeRepository.existsByRequesterIdAndTargetIdAndStatus(
                me.getId(), target.getId(), ExchangeStatus.PENDING
        );
        if (alreadyPending) {
            throw BizException.badRequest("你已向该用户发起过待处理交换，请等待对方处理");
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

    //接受交换请求
    public DiaryExchange acceptExchange(Long exchangeId, Long targetDiaryId) {
        User me = currentUser();

        if (exchangeId == null) {
            throw BizException.badRequest("exchangeId 不能为空");
        }
        if (targetDiaryId == null) {
            throw BizException.badRequest("targetDiaryId 不能为空");
        }

        DiaryExchange ex = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> BizException.notFound("交换记录不存在"));

        // 必须是接收方才能操作
        if (ex.getTarget() == null || !ex.getTarget().getId().equals(me.getId())) {
            throw BizException.forbidden("无权限操作该交换");
        }

        if (ex.getStatus() != ExchangeStatus.PENDING) {
            throw BizException.badRequest("该交换已处理，不能重复操作");
        }

        Diary myDiary = diaryRepository.findById(targetDiaryId)
                .orElseThrow(() -> BizException.notFound("回赠日记不存在"));

        // 只能用自己的日记
        if (myDiary.getUser() == null || !myDiary.getUser().getId().equals(me.getId())) {
            throw BizException.forbidden("不能用别人的日记作为回赠");
        }

        if (myDiary.getVisibility() != Visibility.SEMI_PRIVATE) {
            throw BizException.badRequest("回赠日记必须是 SEMI_PRIVATE");
        }

        ex.setTargetDiary(myDiary);
        ex.setStatus(ExchangeStatus.ACCEPTED);
        ex.setUpdatedAt(LocalDateTime.now());

        return exchangeRepository.save(ex);
    }

    //拒绝交换请求
    public DiaryExchange rejectExchange(Long exchangeId) {
        User me = currentUser();

        if (exchangeId == null) {
            throw BizException.badRequest("exchangeId 不能为空");
        }

        DiaryExchange ex = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> BizException.notFound("交换记录不存在"));

        if (ex.getTarget() == null || !ex.getTarget().getId().equals(me.getId())) {
            throw BizException.forbidden("无权限操作该交换");
        }

        if (ex.getStatus() != ExchangeStatus.PENDING) {
            throw BizException.badRequest("该交换已处理，不能重复操作");
        }

        ex.setStatus(ExchangeStatus.REJECTED);
        ex.setUpdatedAt(LocalDateTime.now());

        return exchangeRepository.save(ex);
    }

    //交换历史：用户参与过的全部
    public List<DiaryExchange> history() {
        User me = currentUser();
        return exchangeRepository.findByRequesterOrTargetOrderByUpdatedAtDesc(me, me);
    }
}
