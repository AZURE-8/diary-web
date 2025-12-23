package com.diaryweb.demo.service;

import com.diaryweb.demo.common.BizException;
import com.diaryweb.demo.entity.Achievement;
import com.diaryweb.demo.entity.User;
import com.diaryweb.demo.entity.UserAchievement;
import com.diaryweb.demo.entity.UserExperience;
import com.diaryweb.demo.repository.AchievementRepository;
import com.diaryweb.demo.repository.UserAchievementRepository;
import com.diaryweb.demo.repository.UserExperienceRepository;
import com.diaryweb.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // 每 100 exp 升 1 级
    private int calcLevel(int exp) {
        return Math.max(1, exp / 100 + 1);
    }

    @Transactional
    public void award(Long userId, int deltaExp, String reason) {
        if (userId == null) throw BizException.badRequest("userId 不能为空");
        if (deltaExp <= 0) return; // deltaExp 不合法直接忽略

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BizException.notFound("用户不存在"));

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

        // 解锁成就
        List<Achievement> all = achievementRepository.findAll();
        for (Achievement a : all) {
            Integer threshold = a.getExpThreshold();
            if (threshold == null) continue;

            if (newExp >= threshold) {
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
