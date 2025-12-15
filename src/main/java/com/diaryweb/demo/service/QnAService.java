package com.diaryweb.demo.service;

import com.diaryweb.demo.entity.Answer;
import com.diaryweb.demo.entity.Question;
import com.diaryweb.demo.entity.User;
import com.diaryweb.demo.repository.AnswerRepository;
import com.diaryweb.demo.repository.QuestionRepository;
import com.diaryweb.demo.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QnAService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final ExperienceService experienceService; // 后面 Part 5 会实现

    public QnAService(QuestionRepository questionRepository,
                      AnswerRepository answerRepository,
                      UserRepository userRepository,
                      ExperienceService experienceService) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
        this.experienceService = experienceService;
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userRepository.findByUsername(username);
        if (u == null) throw new RuntimeException("当前用户不存在");
        return u;
    }

    // 提问
    public Question ask(String content, boolean anonymous) {
        if (content == null || content.isBlank()) {
            throw new RuntimeException("问题内容不能为空");
        }

        User me = currentUser();

        Question q = new Question();
        q.setUser(me);                  // 记录真实提问者（便于管理）
        q.setContent(content);
        q.setAnonymous(anonymous);
        q.setCreatedAt(LocalDateTime.now());

        Question saved = questionRepository.save(q);

        // 提问也可以给少量经验（可选）
        experienceService.award(me.getId(), 2, "ASK_QUESTION");

        return saved;
    }

    // 回答
    public Answer answer(Long questionId, String content, boolean anonymous) {
        if (content == null || content.isBlank()) {
            throw new RuntimeException("回答内容不能为空");
        }

        User me = currentUser();

        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("问题不存在"));

        Answer a = new Answer();
        a.setQuestion(q);
        a.setUser(me);
        a.setContent(content);
        a.setAnonymous(anonymous);
        a.setCreatedAt(LocalDateTime.now());

        Answer saved = answerRepository.save(a);

        // 回答给经验（第二天重点）
        experienceService.award(me.getId(), 5, "ANSWER_QUESTION");

        return saved;
    }

    // 查看问题列表（可用于树洞首页）
    public List<Question> listQuestions() {
        return questionRepository.findAll();
    }

    // 查看某问题的回答
    public List<Answer> listAnswers(Long questionId) {
        return answerRepository.findByQuestionIdOrderByCreatedAtAsc(questionId);
    }
}
