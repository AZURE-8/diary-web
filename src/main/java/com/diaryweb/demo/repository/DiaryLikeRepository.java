package com.diaryweb.demo.repository;

import com.diaryweb.demo.entity.DiaryLike;
import org.springframework.data.jpa.repository.JpaRepository;

//日记点赞数据访问层(点赞记录增删查）
public interface DiaryLikeRepository extends JpaRepository<DiaryLike, Long> {
    boolean existsByDiaryIdAndUserId(Long diaryId, Long userId); //是否点过赞
    long countByDiaryId(Long diaryId); //统计点赞数
    void deleteByDiaryIdAndUserId(Long diaryId, Long userId); //取消点赞
}
