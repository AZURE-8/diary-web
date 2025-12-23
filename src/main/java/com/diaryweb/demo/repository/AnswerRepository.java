package com.diaryweb.demo.repository;

import com.diaryweb.demo.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//树洞回答数据访问层
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestionIdOrderByCreatedAtAsc(Long questionId);
}
