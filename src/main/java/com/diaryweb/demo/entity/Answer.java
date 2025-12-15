package com.diaryweb.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Question question;

    // 回答者（建议保留）
    @ManyToOne
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 回答是否匿名（可选亮点）
    @Column(nullable = false)
    private boolean anonymous = true;

    private LocalDateTime createdAt;

    // getters/setters
    public Long getId() { return id; }
    public Question getQuestion() { return question; }
    public User getUser() { return user; }
    public String getContent() { return content; }
    public boolean isAnonymous() { return anonymous; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setQuestion(Question question) { this.question = question; }
    public void setUser(User user) { this.user = user; }
    public void setContent(String content) { this.content = content; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
