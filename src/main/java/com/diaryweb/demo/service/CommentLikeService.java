package com.diaryweb.demo.service;

import com.diaryweb.demo.entity.*;
import com.diaryweb.demo.repository.CommentRepository;
import com.diaryweb.demo.repository.DiaryLikeRepository;
import com.diaryweb.demo.repository.DiaryRepository;
import com.diaryweb.demo.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentLikeService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final DiaryLikeRepository likeRepository;
    private final ExchangeService exchangeService;

    public CommentLikeService(DiaryRepository diaryRepository,
                              UserRepository userRepository,
                              CommentRepository commentRepository,
                              DiaryLikeRepository likeRepository,
                              ExchangeService exchangeService) {
        this.diaryRepository = diaryRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.exchangeService = exchangeService;
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userRepository.findByUsername(username);
        if (u == null) throw new RuntimeException("当前用户不存在");
        return u;
    }

    //允许互动的条件
    private Diary checkPermissionAndGetDiary(Long diaryId, User me) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("日记不存在"));

        //作者本人直接允许
        if (diary.getUser().getId().equals(me.getId())) {
            return diary;
        }

        //公开日记直接允许
        if (diary.getVisibility() == Visibility.PUBLIC) {
            return diary;
        }

        //半私密日记：检查是否交换过
        if (diary.getVisibility() == Visibility.SEMI_PRIVATE) {
            boolean exchanged = exchangeService.hasAcceptedExchangeBetween(me.getId(), diary.getUser().getId());
            if (exchanged) {
                return diary;
            }
        }

        // 其他情况（如 PRIVATE 或未交换的 SEMI_PRIVATE），禁止
        throw new RuntimeException("您没有权限对该日记进行互动");
    }

    // 添加评论
    @Transactional
    public Comment addComment(Long diaryId, String content) {
        if (content == null || content.isBlank()) {
            throw new RuntimeException("评论内容不能为空");
        }

        User me = currentUser();
        Diary diary = checkPermissionAndGetDiary(diaryId, me);

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

    // 点赞
    @Transactional
    public long like(Long diaryId) {
        User me = currentUser();
        Diary diary = checkPermissionAndGetDiary(diaryId, me);

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
    @Transactional
    public long unlike(Long diaryId) {
        User me = currentUser();
        Diary diary = checkPermissionAndGetDiary(diaryId, me);

        likeRepository.deleteByDiaryIdAndUserId(diary.getId(), me.getId());
        return likeRepository.countByDiaryId(diary.getId());
    }

    // 获取点赞数
    public long likeCount(Long diaryId) {
        return likeRepository.countByDiaryId(diaryId);
    }
    
    // 删除评论
    @Transactional
    public void deleteComment(Long commentId) {
        User me = currentUser();
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        // 只有 "评论发布者" 或 "日记作者" 可以删除
        boolean isAuthor = comment.getUser().getId().equals(me.getId());
        boolean isDiaryOwner = comment.getDiary().getUser().getId().equals(me.getId());

        if (!isAuthor && !isDiaryOwner) {
            throw new RuntimeException("你没有权限删除这条评论");
        }

        commentRepository.delete(comment);
    }
}