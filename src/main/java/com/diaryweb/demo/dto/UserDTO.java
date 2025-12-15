package com.diaryweb.demo.dto;

import com.diaryweb.demo.entity.User;

/**
 * 用户对外展示 DTO
 * 只包含安全、必要的字段
 */
public class UserDTO {

    private Long id;
    private String username;
    private String avatarUrl;
    private String bio;

    public UserDTO() {
    }

    public UserDTO(Long id, String username, String avatarUrl, String bio) {
        this.id = id;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
    }

    /**
     * Entity -> DTO 转换
     */
    public static UserDTO from(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setAvatarUrl(user.getAvatarUrl()); // 如果你的 User 没有这个字段，就删掉这一行
        dto.setBio(user.getBio());             // 如果你的 User 没有这个字段，就删掉这一行
        return dto;
    }

    /* getters & setters */

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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
