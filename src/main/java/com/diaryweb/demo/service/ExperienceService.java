package com.diaryweb.demo.service;

import com.diaryweb.demo.entity.*;
import com.diaryweb.demo.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExperienceService {

    private final UserRepository userRepository;
    private final UserExperienceRepository userExperienceRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;

    public ExperienceService(UserRepository userRepository,
                             UserExperienceRepository userExperienceRepository,
                             AchievementRepository achievementRepository,
                             UserAchievementRepository userAchievementRepository) {
        this.userRepository = userRepository;
        this.userExperienceRepository = userExperienceRepository;
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
    }

    // 经验规则：每 100 exp 升 1 级（简单可解释）
    private int calcLevel(int exp) {
        return Math.max(1, exp / 100 + 1);
    }

    // 统一入口：给某用户加经验 + 检查成就
    public void award(Long userId, int deltaExp, String reason) {
        if (deltaExp <= 0) return;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        UserExperience ux = userExperienceRepository.findById(userId).orElse(null);
        if (ux == null) {
            ux = new UserExperience();
            ux.setUser(user);
            ux.setExp(0);
            ux.setLevel(1);
        }

        int newExp = ux.getExp() + deltaExp;
        ux.setExp(newExp);
        ux.setLevel(calcLevel(newExp));

        userExperienceRepository.save(ux);

        // 解锁成就：根据 expThreshold（最简单版本）
        List<Achievement> all = achievementRepository.findAll();
        for (Achievement a : all) {
            Integer threshold = a.getExpThreshold();
            if (threshold != null && newExp >= threshold) {
                boolean already = userAchievementRepository.existsByUserIdAndAchievementId(userId, a.getId());
                if (!already) {
                    UserAchievement ua = new UserAchievement();
                    ua.setUser(user);
                    ua.setAchievement(a);
                    ua.setUnlockedAt(LocalDateTime.now());
                    userAchievementRepository.save(ua);
                }
            }
        }
    }
}
