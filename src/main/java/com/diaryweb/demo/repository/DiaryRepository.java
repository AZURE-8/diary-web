package com.diaryweb.demo.repository;

import com.diaryweb.demo.entity.Diary;
import com.diaryweb.demo.entity.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    List<Diary> findByUserId(Long userId);

    // 查某用户可见性为 PUBLIC 的日记
    List<Diary> findByUserIdAndVisibility(Long userId, Visibility visibility);

    // 根据标签名搜索（公开日记）
    List<Diary> findByVisibilityAndTags_Name(Visibility visibility, String tagName);
    
    Page<Diary> findByUserId(Long userId, Pageable pageable);

    Page<Diary> findByUserIdAndVisibility(Long userId, Visibility visibility, Pageable pageable);

    Page<Diary> findByVisibility(Visibility visibility, Pageable pageable);

    // 标签搜索（公开日记）
    Page<Diary> findByVisibilityAndTags_Name(Visibility visibility, String tagName, Pageable pageable);

    // 关键字搜索（标题/内容，公开日记）
    Page<Diary> findByVisibilityInAndTitleContainingIgnoreCaseOrVisibilityInAndContentContainingIgnoreCase(
    		List<Visibility> v1, String titleKeyword,
    		List<Visibility> v2, String contentKeyword,
            Pageable pageable
    );

}
