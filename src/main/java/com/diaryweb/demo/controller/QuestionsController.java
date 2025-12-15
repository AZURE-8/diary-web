package com.diaryweb.demo.controller;

import com.diaryweb.demo.common.ApiResponse;
import com.diaryweb.demo.dto.QuestionDTO;
import com.diaryweb.demo.entity.Question;
import com.diaryweb.demo.service.QnAService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionsController {

    private final QnAService qnaService;

    public QuestionsController(QnAService qnaService) {
        this.qnaService = qnaService;
    }

    public static class AskRequest {
        public String content;
        public boolean anonymous = true;
    }

    @PostMapping("/ask")
    public ApiResponse<QuestionDTO> ask(@RequestBody AskRequest req) {
        Question q = qnaService.ask(req.content, req.anonymous);
        return ApiResponse.ok(QuestionDTO.from(q));
    }

    @GetMapping
    public ApiResponse<List<QuestionDTO>> list() {
        List<Question> list = qnaService.listQuestions();
        return ApiResponse.ok(list.stream().map(QuestionDTO::from).toList());
    }
}
