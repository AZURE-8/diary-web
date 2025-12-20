package com.diaryweb.demo.repository;

import com.diaryweb.demo.entity.Diary;
import com.diaryweb.demo.entity.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    List<Diary> findByUserId(Long userId);
    List<Diary> findByUserIdAndVisibility(Long userId, Visibility visibility);
    Page<Diary> findByUserId(Long userId, Pageable pageable);
    
    // 原有的基础查询保留
    Page<Diary> findByUserIdAndVisibility(Long userId, Visibility visibility, Pageable pageable);

    Page<Diary> findByVisibility(Visibility visibility, Pageable pageable);

    // 原有的全站搜索保留...
    @Query("SELECT DISTINCT d FROM Diary d " +
           "LEFT JOIN d.user u " +
           "LEFT JOIN d.tags t " +
           "WHERE d.visibility IN :visibilities " +
           "AND (" +
           "   LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           ")")
    Page<Diary> searchDiaries(
            @Param("visibilities") List<Visibility> visibilities,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // ✅ [新增] 个人主页搜索接口：限定 User ID + 可见性 + 关键词
    @Query("SELECT d FROM Diary d " +
           "WHERE d.user.id = :userId " +
           "AND d.visibility = :visibility " +
           "AND (" +
           "   LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           ")")
    Page<Diary> searchUserPublicDiaries(
            @Param("userId") Long userId, 
            @Param("visibility") Visibility visibility, 
            @Param("keyword") String keyword, 
            Pageable pageable);
}