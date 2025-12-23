package com.diaryweb.demo.controller;

import com.diaryweb.demo.common.ApiResponse;
import com.diaryweb.demo.dto.UserDTO;
import com.diaryweb.demo.dto.UserProfileDTO;
import com.diaryweb.demo.entity.User;
import com.diaryweb.demo.entity.UserExperience;
import com.diaryweb.demo.repository.UserExperienceRepository;
import com.diaryweb.demo.repository.UserRepository;
import com.diaryweb.demo.service.StorageService; 
import com.diaryweb.demo.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; 

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserExperienceRepository userExperienceRepository;
    private final StorageService storageService; // 注入存储服务

    public UserController(UserService userService, 
                          UserRepository userRepository,
                          UserExperienceRepository userExperienceRepository,
                          StorageService storageService) { 
        this.userService = userService;
        this.userRepository = userRepository;
        this.userExperienceRepository = userExperienceRepository;
        this.storageService = storageService;
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileDTO> getMyProfile() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userRepository.findByUsername(currentUsername);
        
        UserProfileDTO profile = new UserProfileDTO();
        if (u == null) {
            UserDTO tempUser = new UserDTO();
            tempUser.setUsername(currentUsername);
            tempUser.setId(-1L);
            profile.setUser(tempUser);
            profile.setExp(0);
            profile.setLevel(1);
            return ApiResponse.ok(profile);
        }

        UserExperience ux = userExperienceRepository.findById(u.getId()).orElse(null);
        profile.setUser(UserDTO.from(u));
        profile.setExp(ux != null ? ux.getExp() : 0);
        profile.setLevel(ux != null ? ux.getLevel() : 1);
        return ApiResponse.ok(profile);
    }

    public static class UpdateUserRequest {
        public String username;
        public String password;
        public String email;
        public String avatarUrl;
        public String bio;
    }

    //用于上传头像的接口
    @PostMapping("/upload/avatar")
    public ApiResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String url = storageService.saveImage(file);
        return ApiResponse.ok(url);
    }

    //个人信息
    @PutMapping("/{userId}")
    public ApiResponse<UserDTO> updateUser(@PathVariable Long userId, @RequestBody UpdateUserRequest req) {
        User updated = userService.updateFullProfile(userId, req.username, req.password, req.email, req.avatarUrl, req.bio);
        return ApiResponse.ok(UserDTO.from(updated));
    }
    

    // 获取他人的公开资料（用于他人主页）
    @GetMapping("/{userId}/public-profile")
    public ApiResponse<UserProfileDTO> getUserPublicProfile(@PathVariable Long userId) {
        User u = userRepository.findById(userId).orElse(null);
        if (u == null) {
            return ApiResponse.fail(404, "用户不存在");
        }

        UserExperience ux = userExperienceRepository.findById(userId).orElse(null);

        UserProfileDTO profile = new UserProfileDTO();
        profile.setUser(UserDTO.from(u)); 
        profile.setExp(ux != null ? ux.getExp() : 0);
        profile.setLevel(ux != null ? ux.getLevel() : 1);

        return ApiResponse.ok(profile);
    }
}