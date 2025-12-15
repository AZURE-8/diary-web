package com.diaryweb.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "diary_exchange")
public class DiaryExchange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 发起方
    @ManyToOne(optional = false)
    private User requester;

    // 接收方
    @ManyToOne(optional = false)
    private User target;

    // 发起方提供的日记（必须是 SEMI_PRIVATE）
    @ManyToOne(optional = false)
    private Diary requesterDiary;

    // 接收方回赠的日记（同意时必须提供，必须是 SEMI_PRIVATE）
    @ManyToOne
    private Diary targetDiary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExchangeStatus status = ExchangeStatus.PENDING;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // getters/setters
    public Long getId() { return id; }
    public User getRequester() { return requester; }
    public User getTarget() { return target; }
    public Diary getRequesterDiary() { return requesterDiary; }
    public Diary getTargetDiary() { return targetDiary; }
    public ExchangeStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setRequester(User requester) { this.requester = requester; }
    public void setTarget(User target) { this.target = target; }
    public void setRequesterDiary(Diary requesterDiary) { this.requesterDiary = requesterDiary; }
    public void setTargetDiary(Diary targetDiary) { this.targetDiary = targetDiary; }
    public void setStatus(ExchangeStatus status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
