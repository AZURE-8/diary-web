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

    // 返回 DTO（避免返回 Entity 导致 user/diary 嵌套）
    public static class ExchangeDTO {
        public Long id;
        public Long requesterId;
        public Long targetId;
        public Long requesterDiaryId;
        public Long targetDiaryId;
        public String status;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;

        public static ExchangeDTO from(DiaryExchange ex) {
            ExchangeDTO dto = new ExchangeDTO();
            dto.id = ex.getId();
            dto.requesterId = ex.getRequester() == null ? null : ex.getRequester().getId();
            dto.targetId = ex.getTarget() == null ? null : ex.getTarget().getId();
            dto.requesterDiaryId = ex.getRequesterDiary() == null ? null : ex.getRequesterDiary().getId();
            dto.targetDiaryId = ex.getTargetDiary() == null ? null : ex.getTargetDiary().getId();
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

    @PostMapping("/{exchangeId}/accept")
    public ApiResponse<ExchangeDTO> accept(@PathVariable Long exchangeId, @RequestBody AcceptRequest req) {
        DiaryExchange ex = exchangeService.acceptExchange(exchangeId, req.targetDiaryId);
        return ApiResponse.ok(ExchangeDTO.from(ex));
    }

    @PostMapping("/{exchangeId}/reject")
    public ApiResponse<ExchangeDTO> reject(@PathVariable Long exchangeId) {
        DiaryExchange ex = exchangeService.rejectExchange(exchangeId);
        return ApiResponse.ok(ExchangeDTO.from(ex));
    }

    @GetMapping("/history")
    public ApiResponse<List<ExchangeDTO>> history() {
        List<DiaryExchange> list = exchangeService.history();
        return ApiResponse.ok(list.stream().map(ExchangeDTO::from).toList());
    }
}
