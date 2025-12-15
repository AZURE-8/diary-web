package com.diaryweb.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_achievements",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "achievement_id"})
)
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Achievement achievement;

    private LocalDateTime unlockedAt;

    // getters/setters
    public Long getId() { return id; }
    public User getUser() { return user; }
    public Achievement getAchievement() { return achievement; }
    public LocalDateTime getUnlockedAt() { return unlockedAt; }

    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setAchievement(Achievement achievement) { this.achievement = achievement; }
    public void setUnlockedAt(LocalDateTime unlockedAt) { this.unlockedAt = unlockedAt; }
}
