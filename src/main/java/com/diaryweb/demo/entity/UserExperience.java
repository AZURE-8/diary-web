package com.diaryweb.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_experience")
public class UserExperience {

    @Id
    private Long userId;     // 直接用 userId 做主键（一对一）

    @OneToOne
    @MapsId
    private User user;

    @Column(nullable = false)
    private int exp = 0;

    @Column(nullable = false)
    private int level = 1;

    // getters/setters
    public Long getUserId() { return userId; }
    public User getUser() { return user; }
    public int getExp() { return exp; }
    public int getLevel() { return level; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setUser(User user) { this.user = user; }
    public void setExp(int exp) { this.exp = exp; }
    public void setLevel(int level) { this.level = level; }
}
