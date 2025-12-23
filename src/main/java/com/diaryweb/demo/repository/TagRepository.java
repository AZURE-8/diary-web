package com.diaryweb.demo.repository;

import com.diaryweb.demo.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

//标签数据访问层
public interface TagRepository extends JpaRepository<Tag, Long> {
    Tag findByName(String name);
}
