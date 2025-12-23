package com.diaryweb.demo.repository;

import com.diaryweb.demo.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//评论数据访问层接口
public interface CommentRepository extends JpaRepository<Comment, Long> {
	//统计评论
	long countByDiaryId(Long diaryId);
	List<Comment> findByDiaryIdOrderByCreatedAtAsc(Long diaryId);
}
