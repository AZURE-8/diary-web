package com.diaryweb.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class StorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    // 保存文件，返回可访问的 URL 路径
    public String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        try {
            String original = StringUtils.cleanPath(file.getOriginalFilename());
            String ext = "";
            int idx = original.lastIndexOf(".");
            if (idx >= 0) ext = original.substring(idx);

            String filename = UUID.randomUUID() + ext;

            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);

            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());

            // 返回一个统一的访问路径（后面再做静态映射）
            return "/uploads/" + filename;
        } catch (Exception e) {
            throw new RuntimeException("图片保存失败: " + e.getMessage(), e);
        }
    }
}
