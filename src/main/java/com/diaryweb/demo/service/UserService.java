package com.diaryweb.demo.service;

import com.diaryweb.demo.common.BizException;
import com.diaryweb.demo.entity.User;
import com.diaryweb.demo.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 获取当前登录的用户对象
     */
    public User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getName();

        if (username == null || username.isBlank() || "anonymousUser".equals(username)) {
            throw new BizException(4010, "未登录或 token 无效");
        }

        User u = userRepository.findByUsername(username);
        if (u == null) {
            throw new BizException(4010, "当前用户不存在");
        }
        return u;
    }

    /**
     * 用户注册
     */
    public User register(String username, String rawPassword, String email) {
        if (username == null || username.trim().isEmpty()) {
            throw BizException.badRequest("用户名不能为空");
        }
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw BizException.badRequest("密码不能为空");
        }

        String uname = username.trim();

        if (userRepository.findByUsername(uname) != null) {
            throw BizException.badRequest("用户名已存在");
        }

        if (email != null && !email.trim().isEmpty()) {
            String em = email.trim();
            if (userRepository.findByEmail(em) != null) {
                throw BizException.badRequest("邮箱已被使用");
            }
        }

        User user = new User();
        user.setUsername(uname);
        user.setPassword(passwordEncoder.encode(rawPassword.trim()));
        user.setEmail(email == null ? null : email.trim());

        return userRepository.save(user);
    }

    /**
     * 根据用户名查找用户
     */
    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw BizException.badRequest("username 不能为空");
        }
        User u = userRepository.findByUsername(username.trim());
        if (u == null) throw BizException.notFound("用户不存在");
        return u;
    }

    /**
     * ✅ 完整资料更新接口
     * 支持：用户名、密码、邮箱、头像、简介
     * 只有本人可以修改自己的信息
     */
    @Transactional
    public User updateFullProfile(Long userId, String username, String rawPassword, String email, String avatarUrl, String bio) {
        if (userId == null) {
            throw BizException.badRequest("userId 不能为空");
        }

        User me = currentUser();
        
        // 核心权限检查：必须是本人操作
        if (!Objects.equals(me.getId(), userId)) {
            throw BizException.forbidden("无权限修改他人信息");
        }

        // 1) 更新用户名：需校验唯一性
        if (username != null && !username.trim().isEmpty()) {
            String newName = username.trim();
            if (!newName.equals(me.getUsername())) {
                User existing = userRepository.findByUsername(newName);
                if (existing != null) {
                    throw BizException.badRequest("用户名已存在");
                }
                me.setUsername(newName);
            }
        }

        // 2) 更新密码：传入才更新
        if (rawPassword != null && !rawPassword.trim().isEmpty()) {
            me.setPassword(passwordEncoder.encode(rawPassword.trim()));
        }

        // 3) 更新邮箱：需校验唯一性
        if (email != null && !email.trim().isEmpty()) {
            String newEmail = email.trim();
            if (!newEmail.equals(me.getEmail())) {
                User existingEmailUser = userRepository.findByEmail(newEmail);
                if (existingEmailUser != null) {
                    throw BizException.badRequest("邮箱已被使用");
                }
                me.setEmail(newEmail);
            }
        }

        // 4) 更新头像和简介
        if (avatarUrl != null) {
            me.setAvatarUrl(avatarUrl.trim());
        }
        if (bio != null) {
            me.setBio(bio.trim());
        }

        return userRepository.save(me);
    }
}