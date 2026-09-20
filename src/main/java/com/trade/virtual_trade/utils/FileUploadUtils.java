package com.trade.virtual_trade.utils;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FileUploadUtils {
    // 上传根路径（配置在 application.yml 中）
    private static String uploadPath = System.getProperty("user.dir") + "/uploads/";

    public static String upload(MultipartFile file, String subDir) throws IOException {
        // 创建目录
        File dir = new File(uploadPath + subDir);
        if (!dir.exists()) dir.mkdirs();

        // 生成新文件名
        String originalName = file.getOriginalFilename();
        String ext = originalName.substring(originalName.lastIndexOf("."));
        String newName = UUID.randomUUID().toString() + ext;

        // 保存文件
        File dest = new File(dir, newName);
        file.transferTo(dest);

        // 返回访问路径（需要配置静态资源映射）
        return "/uploads/" + subDir + "/" + newName;
    }
}