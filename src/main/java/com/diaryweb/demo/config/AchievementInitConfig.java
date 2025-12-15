package com.diaryweb.demo.config;

import com.diaryweb.demo.entity.Achievement;
import com.diaryweb.demo.repository.AchievementRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AchievementInitConfig {

    @Bean
    public CommandLineRunner initAchievements(AchievementRepository repo) {
        return args -> {
            if (repo.findByCode("EXP_10") == null) {
                Achievement a = new Achievement();
                a.setCode("EXP_10");
                a.setTitle("新手上路");
                a.setDescription("累计经验达到 10");
                a.setExpThreshold(10);
                repo.save(a);
            }

            if (repo.findByCode("EXP_50") == null) {
                Achievement a = new Achievement();
                a.setCode("EXP_50");
                a.setTitle("坚持记录");
                a.setDescription("累计经验达到 50");
                a.setExpThreshold(50);
                repo.save(a);
            }

            if (repo.findByCode("EXP_100") == null) {
                Achievement a = new Achievement();
                a.setCode("EXP_100");
                a.setTitle("达人");
                a.setDescription("累计经验达到 100");
                a.setExpThreshold(100);
                repo.save(a);
            }
        };
    }
}
