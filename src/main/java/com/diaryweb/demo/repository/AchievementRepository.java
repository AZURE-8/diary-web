package com.diaryweb.demo.repository;

import com.diaryweb.demo.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

//成就数据访问层接口
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    Achievement findByCode(String code);
}
