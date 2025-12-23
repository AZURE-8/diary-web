package com.diaryweb.demo.repository;

import com.diaryweb.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

//用户数据访问层接口
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);  // 根据用户名查找用户
    User findByEmail(String email);  // 根据邮箱查找用户
}