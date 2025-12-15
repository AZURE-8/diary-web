package com.diaryweb.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "likes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"diary_id", "user_id"})
)
public class DiaryLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Diary diary;

    @ManyToOne(optional = false)
    private User user;

    // getters/setters
    public Long getId() { return id; }
    public Diary getDiary() { return diary; }
    public User getUser() { return user; }

    public void setId(Long id) { this.id = id; }
    public void setDiary(Diary diary) { this.diary = diary; }
    public void setUser(User user) { this.user = user; }
}
