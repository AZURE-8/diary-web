package com.diaryweb.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 提问者（可为空：如果你希望真正匿名不存用户也可以；这里建议保留以便风控）
    @ManyToOne
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 匿名标志：true 表示前端展示“匿名”，false 展示用户名
    @Column(nullable = false)
    private boolean anonymous = true;

    private LocalDateTime createdAt;
    
 // src/main/java/com/diaryweb/demo/entity/Question.java
    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
    private java.util.List<Answer> answers; 

    public java.util.List<Answer> getAnswers() { return answers; }

    // getters/setters
    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getContent() { return content; }
    public boolean isAnonymous() { return anonymous; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setContent(String content) { this.content = content; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
}
