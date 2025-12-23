package com.diaryweb.demo.repository;

import com.diaryweb.demo.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

//树洞问题数据访问层
public interface QuestionRepository extends JpaRepository<Question, Long> {
}
