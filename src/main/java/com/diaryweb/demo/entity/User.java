package com.diaryweb.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名（唯一，用于登录）
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * 存储 BCrypt 加密后的密码
     */
    @Column(nullable = false)
    private String password;

    /**
     * 邮箱（唯一，可用于找回密码 / 社交登录绑定）
     */
    @Column(unique = true)
    private String email;

    /**
     * 用户头像地址（本地路径或 URL）
     */
    private String avatarUrl;

    /**
     * 账号创建时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 最近一次更新时间
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 实体持久化前自动设置时间
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 实体更新前自动更新时间
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Column(length = 500)
    private String bio;

    // ===================== Getter & Setter =====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 注意：此处只允许设置“加密后的密码”
     */
    public String getPassword() {
        return password;
    }

    /**
     * Service 层必须先加密再调用此方法
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

}
