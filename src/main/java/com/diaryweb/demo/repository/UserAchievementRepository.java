package com.diaryweb.demo.repository;

import com.diaryweb.demo.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//用户成就关联数据访问层
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    boolean existsByUserIdAndAchievementId(Long userId, Long achievementId);
    List<UserAchievement> findByUserId(Long userId);
}
