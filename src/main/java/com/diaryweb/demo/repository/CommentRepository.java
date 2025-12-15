package com.diaryweb.demo.repository;

import com.diaryweb.demo.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByDiaryIdOrderByCreatedAtAsc(Long diaryId);
}
