package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/admin/common")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;
    @PostMapping("/upload")
    //参数是file类型，前端上传文件时，参数名必须是file
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("文件上传");
        String originalFilename = file.getOriginalFilename();
        String substring = originalFilename.substring(originalFilename.lastIndexOf("."));
        String name=UUID.randomUUID().toString()+substring;
        String upload2 = aliOssUtil.upload(file.getBytes(), name);


        return Result.success(upload2);
    }



}
