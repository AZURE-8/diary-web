package com.diaryweb.demo.repository;

import com.diaryweb.demo.entity.DiaryLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryLikeRepository extends JpaRepository<DiaryLike, Long> {
    boolean existsByDiaryIdAndUserId(Long diaryId, Long userId);
    long countByDiaryId(Long diaryId);
    void deleteByDiaryIdAndUserId(Long diaryId, Long userId);
}
