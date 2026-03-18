package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliyunOSSOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/admin/common")
public class CommonController {

    @Autowired
    private AliyunOSSOperator aliyunOSSOperator;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        log.info("文件上传: {}", file.getOriginalFilename());

        try {
            // 1. 获取文件的原始名称（如 abc.jpg）
            String originalFilename = file.getOriginalFilename();

            // 2. 将文件的字节数组和原始名称直接交给工具类处理
            // 工具类内部已经实现了安全获取后缀、生成 UUID、拼接日期目录的完整逻辑
            String fileUrl = aliyunOSSOperator.upload(file.getBytes(), originalFilename);

            // 3. 返回上传成功后的访问 URL
            return Result.success(fileUrl);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            // 捕获异常并返回失败提示，避免前端收到生硬的 500 状态码
            return Result.error("文件上传失败");
        }
    }
}