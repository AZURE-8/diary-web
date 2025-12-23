package com.diaryweb.demo.repository;

import com.diaryweb.demo.entity.Diary;
import com.diaryweb.demo.entity.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

//日记数据访问层
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    List<Diary> findByUserId(Long userId);
    List<Diary> findByUserIdAndVisibility(Long userId, Visibility visibility);
    Page<Diary> findByUserId(Long userId, Pageable pageable);
    
    Page<Diary> findByUserIdAndVisibility(Long userId, Visibility visibility, Pageable pageable);

    Page<Diary> findByVisibility(Visibility visibility, Pageable pageable);

    //全局模糊搜索(公开和半私密的标题、内容、作者用户名、标签名)
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

    //个人主页搜索(UserID + 可见性 + 关键词)
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