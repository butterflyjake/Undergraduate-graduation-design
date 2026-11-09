package com.campus.aed.controller;

import com.campus.aed.common.BusinessException;
import com.campus.aed.common.Result;
import com.campus.aed.vo.FileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传控制器: 设备照片/故障照片等图片上传
 */
@RestController
@RequestMapping("/api/file")
@Tag(name = "文件上传")
public class FileController {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Value("${upload.path:./uploads/}")
    private String uploadPath;

    @PostMapping("/upload")
    @Operation(summary = "上传图片")
    public Result<FileVO> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        }
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException("仅支持 jpg/jpeg/png/gif/webp 格式的图片");
        }
        try {
            Path dir = Paths.get(uploadPath).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String filename = UUID.randomUUID() + "." + ext;
            file.transferTo(dir.resolve(filename).toFile());
            return Result.ok(new FileVO("/upload/" + filename));
        } catch (IOException e) {
            throw new BusinessException("文件保存失败, 请重试");
        }
    }
}
