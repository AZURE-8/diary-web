package com.diaryweb.demo.repository;

import com.diaryweb.demo.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    Achievement findByCode(String code);
}
