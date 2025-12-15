package com.diaryweb.demo.repository;

import com.diaryweb.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);  // 根据用户名查找用户
    User findByEmail(String email);
}