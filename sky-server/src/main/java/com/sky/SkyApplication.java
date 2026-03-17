package com.sky;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
public class SkyApplication {
    public static void main(String[] args) {
        // 检查关键环境变量
        checkEnvironmentVariables();

        SpringApplication.run(SkyApplication.class, args);
        log.info("server started");
    }

    /**
     * 检查必需的环境变量是否已配置
     */
    private static void checkEnvironmentVariables() {
        String ossAccessKeyId = System.getenv("OSS_ACCESS_KEY_ID");
        String ossAccessKeySecret = System.getenv("OSS_ACCESS_KEY_SECRET");

        if (ossAccessKeyId == null || ossAccessKeyId.trim().isEmpty()) {
            log.warn("===========================================");
            log.warn("警告：环境变量 OSS_ACCESS_KEY_ID 未设置！");
            log.warn("文件上传功能将无法使用。");
            log.warn("请参考 docs/环境变量配置指南.md 进行配置");
            log.warn("===========================================");
        }

        if (ossAccessKeySecret == null || ossAccessKeySecret.trim().isEmpty()) {
            log.warn("===========================================");
            log.warn("警告：环境变量 OSS_ACCESS_KEY_SECRET 未设置！");
            log.warn("文件上传功能将无法使用。");
            log.warn("请参考 docs/环境变量配置指南.md 进行配置");
            log.warn("===========================================");
        }

        if (ossAccessKeyId != null && !ossAccessKeyId.trim().isEmpty() &&
                ossAccessKeySecret != null && !ossAccessKeySecret.trim().isEmpty()) {
            log.info("阿里云OSS环境变量配置检查通过");
        }
    }
}
