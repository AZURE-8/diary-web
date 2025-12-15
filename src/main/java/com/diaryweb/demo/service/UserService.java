package com.diaryweb.demo.service;

import com.diaryweb.demo.entity.User;
import com.diaryweb.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;   // 用来加密密码

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 注册
    public User register(String username, String rawPassword, String email) {
        // 0. 基本校验（避免空值导致数据库约束异常）
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }

        // 1. 检查用户名是否已存在
        if (userRepository.findByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 如果你把 email 设置为 unique，这里建议也检查一下（可选但推荐）
        if (email != null && !email.trim().isEmpty()) {
            User existingEmailUser = userRepository.findByEmail(email);
            if (existingEmailUser != null) {
                throw new RuntimeException("邮箱已被使用");
            }
        }

        // 3. 创建用户并保存
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(rawPassword)); // BCrypt 加密
        user.setEmail(email);

        // 关键：不再手动设置 createdAt / updatedAt
        // 由 User 实体中的 @PrePersist / @PreUpdate 自动维护

        return userRepository.save(user);
    }

    // 查找用户（登录时使用）
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // 更新用户信息（示例：允许更新用户名、密码、邮箱）
    public User updateUser(Long userId, String username, String rawPassword, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. 更新用户名：若传入且不同，则需要检查唯一性
        if (username != null && !username.trim().isEmpty()
                && !username.trim().equals(user.getUsername())) {

            if (userRepository.findByUsername(username.trim()) != null) {
                throw new RuntimeException("用户名已存在");
            }
            user.setUsername(username.trim());
        }

        // 2. 更新密码：如果传入新密码，则必须加密后存储
        if (rawPassword != null && !rawPassword.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }

        // 3. 更新邮箱：若你设置了 email unique，建议做唯一性检查
        if (email != null && !email.trim().isEmpty()
                && (user.getEmail() == null || !email.trim().equals(user.getEmail()))) {

            User existingEmailUser = userRepository.findByEmail(email.trim());
            if (existingEmailUser != null && !existingEmailUser.getId().equals(user.getId())) {
                throw new RuntimeException("邮箱已被使用");
            }
            user.setEmail(email.trim());
        }

        // updatedAt 会由 @PreUpdate 自动更新
        return userRepository.save(user);
    }
}
