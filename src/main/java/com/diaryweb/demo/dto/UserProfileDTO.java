package com.diaryweb.demo.dto;

//个人中心数据传输对象
public class UserProfileDTO {
    private UserDTO user;  // 包含基本信息（用户名、头像等）
    private int exp;       // 经验值
    private int level;     // 等级

    public UserProfileDTO() {}

    // Getter 和 Setter
    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }

    public int getExp() { return exp; }
    public void setExp(int exp) { this.exp = exp; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
}