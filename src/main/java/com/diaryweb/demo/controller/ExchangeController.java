package com.diaryweb.demo.controller;

import com.diaryweb.demo.common.ApiResponse;
import com.diaryweb.demo.entity.DiaryExchange;
import com.diaryweb.demo.service.ExchangeService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/diaries/exchange")
public class ExchangeController {

    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    public static class ExchangeRequest {
        public Long targetUserId;
        public Long requesterDiaryId;
    }

    public static class AcceptRequest {
        public Long targetDiaryId;
    }

    public static class ExchangeDTO {
        public Long id;
        public Long requesterId;
        public String requesterName;       // 发起者用户名
        public Long targetId;
        public String targetName;          // 接收者用户名
        public Long requesterDiaryId;
        public String requesterDiaryTitle; // 发起者日记标题
        public Long targetDiaryId;
        public String targetDiaryTitle;    //接收者日记标题（接受后才显示）
        public String status;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;

        public static ExchangeDTO from(DiaryExchange ex) {
            ExchangeDTO dto = new ExchangeDTO();
            dto.id = ex.getId();
            
            // 获取发起者信息
            if (ex.getRequester() != null) {
                dto.requesterId = ex.getRequester().getId();
                dto.requesterName = ex.getRequester().getUsername();
            }
            
            // 获取接收者信息
            if (ex.getTarget() != null) {
                dto.targetId = ex.getTarget().getId();
                dto.targetName = ex.getTarget().getUsername();
            }

            // 获取发起者的日记信息
            if (ex.getRequesterDiary() != null) {
                dto.requesterDiaryId = ex.getRequesterDiary().getId();
                dto.requesterDiaryTitle = ex.getRequesterDiary().getTitle();
            }

            // 获取接收者的日记信息
            if (ex.getTargetDiary() != null) {
                dto.targetDiaryId = ex.getTargetDiary().getId();
                dto.targetDiaryTitle = ex.getTargetDiary().getTitle();
            }

            dto.status = ex.getStatus() == null ? null : ex.getStatus().name();
            dto.createdAt = ex.getCreatedAt();
            dto.updatedAt = ex.getUpdatedAt();
            return dto;
        }
    }

    @PostMapping
    public ApiResponse<ExchangeDTO> request(@RequestBody ExchangeRequest req) {
        DiaryExchange ex = exchangeService.requestExchange(req.targetUserId, req.requesterDiaryId);
        return ApiResponse.ok(ExchangeDTO.from(ex));
    }

    //接受
    @PostMapping("/{exchangeId}/accept")
    public ApiResponse<ExchangeDTO> accept(@PathVariable Long exchangeId, @RequestBody AcceptRequest req) {
        DiaryExchange ex = exchangeService.acceptExchange(exchangeId, req.targetDiaryId);
        return ApiResponse.ok(ExchangeDTO.from(ex));
    }

    //拒绝
    @PostMapping("/{exchangeId}/reject")
    public ApiResponse<ExchangeDTO> reject(@PathVariable Long exchangeId) {
        DiaryExchange ex = exchangeService.rejectExchange(exchangeId);
        return ApiResponse.ok(ExchangeDTO.from(ex));
    }

    //发起和接收历史
    @GetMapping("/history")
    public ApiResponse<List<ExchangeDTO>> history() {
        List<DiaryExchange> list = exchangeService.history();
        return ApiResponse.ok(list.stream().map(ExchangeDTO::from).toList());
    }
}
