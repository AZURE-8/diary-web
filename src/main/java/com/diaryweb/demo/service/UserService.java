package com.diaryweb.demo.service;

import com.diaryweb.demo.common.BizException;
import com.diaryweb.demo.entity.User;
import com.diaryweb.demo.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;   // BCrypt

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private User currentUser() {
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

    // 注册
    public User register(String username, String rawPassword, String email) {
        // 0) 参数校验
        if (username == null || username.trim().isEmpty()) {
            throw BizException.badRequest("用户名不能为空");
        }
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw BizException.badRequest("密码不能为空");
        }

        String uname = username.trim();

        // 1) 用户名唯一
        if (userRepository.findByUsername(uname) != null) {
            throw BizException.badRequest("用户名已存在");
        }

        // 2) 邮箱唯一（可选：你如果库里 email 没 unique，也可保留该检查）
        if (email != null && !email.trim().isEmpty()) {
            String em = email.trim();
            User existingEmailUser = userRepository.findByEmail(em);
            if (existingEmailUser != null) {
                throw BizException.badRequest("邮箱已被使用");
            }
        }

        // 3) 保存用户
        User user = new User();
        user.setUsername(uname);
        user.setPassword(passwordEncoder.encode(rawPassword.trim()));
        user.setEmail(email == null ? null : email.trim());

        // 可选：给默认头像/简介（提升体验）
        if (user.getAvatarUrl() == null) user.setAvatarUrl(null);
        if (user.getBio() == null) user.setBio(null);

        return userRepository.save(user);
    }

    // 查找用户（登录时、或 Controller 查询用户信息时用）
    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw BizException.badRequest("username 不能为空");
        }
        User u = userRepository.findByUsername(username.trim());
        if (u == null) throw BizException.notFound("用户不存在");
        return u;
    }

    /**
     * 更新用户信息（第四天强化：只能更新“本人”）
     *
     * 规则：
     * - 只有本人可以更新自己资料
     * - username/email 若变化，需要做唯一性校验
     * - password 为空则不更新（避免把密码改空）
     */
    public User updateUser(Long userId, String username, String rawPassword, String email) {
        if (userId == null) {
            throw BizException.badRequest("userId 不能为空");
        }

        User me = currentUser();
        if (!Objects.equals(me.getId(), userId)) {
            throw BizException.forbidden("无权限修改他人信息");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BizException.notFound("用户不存在"));

        // 1) 更新用户名：需要唯一性
        if (username != null && !username.trim().isEmpty()) {
            String newName = username.trim();
            if (!newName.equals(user.getUsername())) {
                User existing = userRepository.findByUsername(newName);
                if (existing != null && !Objects.equals(existing.getId(), user.getId())) {
                    throw BizException.badRequest("用户名已存在");
                }
                user.setUsername(newName);
            }
        }

        // 2) 更新密码：传入才更新
        if (rawPassword != null && !rawPassword.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(rawPassword.trim()));
        }

        // 3) 更新邮箱：需要唯一性
        if (email != null && !email.trim().isEmpty()) {
            String newEmail = email.trim();
            if (!Objects.equals(newEmail, user.getEmail())) {
                User existingEmailUser = userRepository.findByEmail(newEmail);
                if (existingEmailUser != null && !Objects.equals(existingEmailUser.getId(), user.getId())) {
                    throw BizException.badRequest("邮箱已被使用");
                }
                user.setEmail(newEmail);
            }
        }

        return userRepository.save(user);
    }

    /**
     * 可选：更新头像/简介（更贴近产品）
     * 如果你的 User 实体有 avatarUrl / bio 字段，建议用这个接口而不是强行塞进 updateUser 的参数里。
     */
    public User updateProfile(Long userId, String avatarUrl, String bio) {
        if (userId == null) throw BizException.badRequest("userId 不能为空");

        User me = currentUser();
        if (!Objects.equals(me.getId(), userId)) {
            throw BizException.forbidden("无权限修改他人信息");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BizException.notFound("用户不存在"));

        if (avatarUrl != null) user.setAvatarUrl(avatarUrl.trim());
        if (bio != null) user.setBio(bio.trim());

        return userRepository.save(user);
    }
}
