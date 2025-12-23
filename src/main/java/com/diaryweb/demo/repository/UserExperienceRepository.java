package com.diaryweb.demo.repository;

import com.diaryweb.demo.entity.UserExperience;
import org.springframework.data.jpa.repository.JpaRepository;

//用户经验值数据访问层(查询经验值和等级)
public interface UserExperienceRepository extends JpaRepository<UserExperience, Long> {
}
