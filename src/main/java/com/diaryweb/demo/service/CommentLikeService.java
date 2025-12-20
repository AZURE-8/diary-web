package com.diaryweb.demo.service;

import com.diaryweb.demo.entity.*;
import com.diaryweb.demo.repository.CommentRepository;
import com.diaryweb.demo.repository.DiaryLikeRepository;
import com.diaryweb.demo.repository.DiaryRepository;
import com.diaryweb.demo.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // [新增] 必须导入

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentLikeService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final DiaryLikeRepository likeRepository;

    public CommentLikeService(DiaryRepository diaryRepository,
                              UserRepository userRepository,
                              CommentRepository commentRepository,
                              DiaryLikeRepository likeRepository) {
        this.diaryRepository = diaryRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userRepository.findByUsername(username);
        if (u == null) throw new RuntimeException("当前用户不存在");
        return u;
    }

    private Diary publicDiaryOrThrow(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("日记不存在"));

        // 只允许对 PUBLIC 日记互动
        if (diary.getVisibility() != Visibility.PUBLIC) {
            throw new RuntimeException("该日记不是公开日记，不能评论/点赞");
        }
        return diary;
    }

    // 添加评论
    @Transactional // [建议] 写操作建议加上事务
    public Comment addComment(Long diaryId, String content) {
        if (content == null || content.isBlank()) {
            throw new RuntimeException("评论内容不能为空");
        }

        User me = currentUser();
        Diary diary = publicDiaryOrThrow(diaryId);

        Comment c = new Comment();
        c.setDiary(diary);
        c.setUser(me);
        c.setContent(content);
        c.setCreatedAt(LocalDateTime.now());

        return commentRepository.save(c);
    }

    // 查看评论
    public List<Comment> listComments(Long diaryId) {
        return commentRepository.findByDiaryIdOrderByCreatedAtAsc(diaryId);
    }

    // 点赞（幂等：重复点赞不报错，直接返回当前点赞数）
    @Transactional // [建议] 涉及先读后写，加上事务更安全
    public long like(Long diaryId) {
        User me = currentUser();
        Diary diary = publicDiaryOrThrow(diaryId);

        boolean exists = likeRepository.existsByDiaryIdAndUserId(diary.getId(), me.getId());
        if (!exists) {
            DiaryLike like = new DiaryLike();
            like.setDiary(diary);
            like.setUser(me);
            likeRepository.save(like);
        }
        return likeRepository.countByDiaryId(diary.getId());
    }

    // 取消点赞
    // 必须加 @Transactional，否则 JPA 执行 deleteBy... 会报“没有事务”的错误
    @Transactional 
    public long unlike(Long diaryId) {
        User me = currentUser();
        Diary diary = publicDiaryOrThrow(diaryId);

        likeRepository.deleteByDiaryIdAndUserId(diary.getId(), me.getId());
        return likeRepository.countByDiaryId(diary.getId());
    }

    // 获取点赞数
    public long likeCount(Long diaryId) {
        return likeRepository.countByDiaryId(diaryId);
    }
    
 // 在 demo/service/CommentLikeService.java 中添加以下方法

    // [新增] 删除评论
    @Transactional
    public void deleteComment(Long commentId) {
        User me = currentUser();
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        // 权限校验：只有 "评论发布者" 或 "日记作者" 可以删除
        boolean isAuthor = comment.getUser().getId().equals(me.getId());
        boolean isDiaryOwner = comment.getDiary().getUser().getId().equals(me.getId());

        if (!isAuthor && !isDiaryOwner) {
            throw new RuntimeException("你没有权限删除这条评论");
        }

        commentRepository.delete(comment);
    }
}