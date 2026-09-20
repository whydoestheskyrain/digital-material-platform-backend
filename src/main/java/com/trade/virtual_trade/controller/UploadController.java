package com.trade.virtual_trade.controller;

import com.trade.virtual_trade.utils.FileUploadUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @PostMapping("/image")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (file.getSize() > 2 * 1024 * 1024) {
                result.put("code", 400);
                result.put("msg", "图片不能超过2MB");
                return result;
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.put("code", 400);
                result.put("msg", "只支持图片格式");
                return result;
            }

            String url = FileUploadUtils.upload(file, "product");
            result.put("code", 200);
            result.put("data", url);
            result.put("msg", "上传成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "上传失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/pet")
    public Map<String, Object> uploadPetSkin(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (file.getSize() > 5 * 1024 * 1024) {
                result.put("code", 400);
                result.put("msg", "文件不能超过5MB");
                return result;
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.put("code", 400);
                result.put("msg", "只支持图片格式");
                return result;
            }

            String url = FileUploadUtils.upload(file, "pet");
            result.put("code", 200);
            result.put("data", url);
            result.put("msg", "上传成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "上传失败：" + e.getMessage());
        }
        return result;
    }
}