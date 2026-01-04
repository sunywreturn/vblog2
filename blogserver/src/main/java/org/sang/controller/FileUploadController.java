package org.sang.controller;

import org.sang.bean.RespBean;
import org.sang.service.UserService;
import org.sang.utils.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 * Created by sang on 2024/01/01
 */
@RestController
@RequestMapping("/upload")
public class FileUploadController {

    @Value("${file.upload.path:/upload/}")
    private String uploadPath;

    @Value("${file.upload.domain:http://localhost:8081}")
    private String uploadDomain;

    @Value("${file.avatar.max-size:2097152}")
    private long avatarMaxSize; // 2MB

    @Autowired
    private UserService userService;

    /**
     * 上传头像
     * @param file 头像文件
     * @return 上传结果
     */
    @PostMapping("/avatar")
    public RespBean uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                return new RespBean("error", "请选择要上传的头像文件");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
                return new RespBean("error", "头像文件只支持 JPG 和 PNG 格式");
            }

            // 验证文件大小
            if (file.getSize() > avatarMaxSize) {
                return new RespBean("error", "头像文件大小不能超过 2MB");
            }

            // 创建上传目录
            String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            String relativePath = uploadPath + "avatar/" + datePath + "/";
            String absolutePath = System.getProperty("user.dir") + relativePath;
            
            File uploadDir = new File(absolutePath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = Paths.get(absolutePath + newFilename);
            Files.write(filePath, file.getBytes());

            // 构建访问URL
            String fileUrl = uploadDomain + "/static/avatar/" + datePath + "/" + newFilename;

            // 更新用户头像
            Long currentUserId = Util.getCurrentUser().getId();
            int result = userService.updateUserAvatar(fileUrl, currentUserId);
            
            if (result > 0) {
                Map<String, Object> data = new HashMap<>();
                data.put("url", fileUrl);
                data.put("message", "头像上传成功");
                return new RespBean("success", "头像上传成功");
            } else {
                return new RespBean("error", "头像上传成功，但更新数据库失败");
            }

        } catch (IOException e) {
            e.printStackTrace();
            return new RespBean("error", "头像上传失败：" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new RespBean("error", "头像上传失败");
        }
    }

    /**
     * 删除旧头像
     * @param avatarUrl 头像URL
     */
    private void deleteOldAvatar(String avatarUrl) {
        try {
            if (avatarUrl != null && !avatarUrl.trim().isEmpty() 
                && !avatarUrl.contains("default.png")) {
                String relativePath = avatarUrl.substring(uploadDomain.length());
                String absolutePath = System.getProperty("user.dir") + relativePath;
                File oldFile = new File(absolutePath);
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}