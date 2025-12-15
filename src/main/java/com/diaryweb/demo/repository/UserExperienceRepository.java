package com.diaryweb.demo.repository;

import com.diaryweb.demo.entity.UserExperience;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserExperienceRepository extends JpaRepository<UserExperience, Long> {
}
