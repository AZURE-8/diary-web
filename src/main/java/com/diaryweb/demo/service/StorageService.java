package com.diaryweb.demo.service;

import com.diaryweb.demo.common.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class StorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.publicBaseUrl:}")
    private String publicBaseUrl;

    // 图片类型白名单
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    // 限制图片大小
    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024;

    // 保存图片，返回可访问的 URL 路径
    public String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        if (file.getSize() > MAX_SIZE_BYTES) {
            throw BizException.badRequest("图片过大，最大允许 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw BizException.badRequest("不支持的图片格式，仅允许 jpg/png/webp/gif");
        }

        try {
            // 原始文件名清洗（防止路径穿越）
            String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());

            // 扩展名
            String ext = "";
            int idx = original.lastIndexOf(".");
            if (idx >= 0 && idx < original.length() - 1) {
                ext = original.substring(idx).toLowerCase();
            }

            // 进一步限制扩展名
            if (!ext.isEmpty() && !(ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png") || ext.equals(".webp") || ext.equals(".gif"))) {
                throw BizException.badRequest("文件扩展名不合法");
            }

            String filename = UUID.randomUUID() + (ext.isEmpty() ? guessExtByContentType(contentType) : ext);

            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            Path target = dir.resolve(filename).normalize();

            // 防止路径穿越
            if (!target.startsWith(dir)) {
                throw BizException.badRequest("非法文件路径");
            }

            // 用绝对路径写入
            file.transferTo(target.toFile());

            // 返回可访问 URL
            String relative = "/uploads/" + filename;
            if (publicBaseUrl == null || publicBaseUrl.isBlank()) return relative;
            return publicBaseUrl + relative;

        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(5000, "图片保存失败: " + e.getMessage());
        }
    }

    private String guessExtByContentType(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> "";
        };
    }
}
