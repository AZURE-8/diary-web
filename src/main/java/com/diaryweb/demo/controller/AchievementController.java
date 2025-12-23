package com.diaryweb.demo.controller;

import com.diaryweb.demo.common.ApiResponse;
import com.diaryweb.demo.entity.UserAchievement;
import com.diaryweb.demo.repository.UserAchievementRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class AchievementController {

    private final UserAchievementRepository userAchievementRepository;

    public AchievementController(UserAchievementRepository userAchievementRepository) {
        this.userAchievementRepository = userAchievementRepository;
    }

    public static class UserAchievementDTO {
        public Long achievementId;
        public String code;
        public String title;
        public String description;
        public LocalDateTime unlockedAt;

        public static UserAchievementDTO from(UserAchievement ua) {
            UserAchievementDTO dto = new UserAchievementDTO();
            dto.unlockedAt = ua.getUnlockedAt();
            if (ua.getAchievement() != null) {
                dto.achievementId = ua.getAchievement().getId();
                dto.code = ua.getAchievement().getCode();
                dto.title = ua.getAchievement().getTitle();
                dto.description = ua.getAchievement().getDescription();
            }
            return dto;
        }
    }

    //成就接口
    @GetMapping("/{userId}/achievements")
    public ApiResponse<List<UserAchievementDTO>> achievements(@PathVariable Long userId) {
        List<UserAchievement> list = userAchievementRepository.findByUserId(userId);
        return ApiResponse.ok(list.stream().map(UserAchievementDTO::from).toList());
    }
}
