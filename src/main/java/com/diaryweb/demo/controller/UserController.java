package com.diaryweb.demo.controller;

import com.diaryweb.demo.common.ApiResponse;
import com.diaryweb.demo.dto.UserDTO;
import com.diaryweb.demo.entity.User;
import com.diaryweb.demo.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 根据用户名查询（用于测试拿 userId）
    @GetMapping("/{username}")
    public ApiResponse<UserDTO> getUser(@PathVariable String username) {
        User u = userService.findByUsername(username);
        return ApiResponse.ok(UserDTO.from(u));
    }

    // 更新用户信息（示例：username/email；建议你后面改成只允许本人改）
    public static class UpdateUserRequest {
        public String username;
        public String email;
        public String avatarUrl;
        public String bio;
    }

    @PutMapping("/{userId}")
    public ApiResponse<UserDTO> updateUser(@PathVariable Long userId, @RequestBody UpdateUserRequest req) {
        User updated = userService.updateUser(userId, req.username, null, req.email);
        // 如果你的 updateUser 支持 avatar/bio，请在 service 里补齐并在这里传入
        return ApiResponse.ok(UserDTO.from(updated));
    }
}
