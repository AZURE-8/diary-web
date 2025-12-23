package com.diaryweb.demo.controller;

import com.diaryweb.demo.common.ApiResponse;
import com.diaryweb.demo.dto.AnswerDTO;
import com.diaryweb.demo.entity.Answer;
import com.diaryweb.demo.service.QnAService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/answers")
public class AnswersController {

    private final QnAService qnaService;

    public AnswersController(QnAService qnaService) {
        this.qnaService = qnaService;
    }

    // 接收回答提交参数的 DTO
    public static class AnswerRequest {
        public Long questionId;
        public String content;
        public boolean anonymous = true;
    }

    //  提交新回答的接口
    @PostMapping("/answer")
    public ApiResponse<AnswerDTO> answer(@RequestBody AnswerRequest req) {
        Answer a = qnaService.answer(req.questionId, req.content, req.anonymous);
        return ApiResponse.ok(AnswerDTO.from(a));
    }

    // 获取所有回答的接口
    @GetMapping("/byQuestion/{questionId}")
    public ApiResponse<List<AnswerDTO>> list(@PathVariable Long questionId) {
        List<Answer> list = qnaService.listAnswers(questionId);
        return ApiResponse.ok(list.stream().map(AnswerDTO::from).toList());
    }
}
