package com.sky.utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.sky.properties.AliOssProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
public class AliyunOSSOperator {

    private final AliOssProperties aliOssProperties;

    public AliyunOSSOperator(AliOssProperties aliOssProperties) {
        this.aliOssProperties = aliOssProperties;
    }

    public String upload(byte[] content, String originalFilename) throws Exception {
        String endpoint = aliOssProperties.getEndpoint();
        String bucketName = aliOssProperties.getBucketName();
        String region = aliOssProperties.getRegion();

        // 验证配置
        if (endpoint == null || endpoint.trim().isEmpty()) {
            log.error("阿里云OSS配置错误：endpoint未配置");
            throw new IllegalArgumentException("阿里云OSS endpoint未配置");
        }
        if (bucketName == null || bucketName.trim().isEmpty()) {
            log.error("阿里云OSS配置错误：bucketName未配置");
            throw new IllegalArgumentException("阿里云OSS bucketName未配置");
        }
        if (region == null || region.trim().isEmpty()) {
            log.error("阿里云OSS配置错误：region未配置");
            throw new IllegalArgumentException("阿里云OSS region未配置");
        }

        // 验证环境变量
        String accessKeyId = System.getenv("OSS_ACCESS_KEY_ID");
        String accessKeySecret = System.getenv("OSS_ACCESS_KEY_SECRET");

        if (accessKeyId == null || accessKeyId.trim().isEmpty()) {
            log.error("环境变量 OSS_ACCESS_KEY_ID 未设置或为空，请参考 docs/环境变量配置指南.md 进行配置");
            throw new IllegalStateException("环境变量 OSS_ACCESS_KEY_ID 未设置，请配置后重启应用");
        }
        if (accessKeySecret == null || accessKeySecret.trim().isEmpty()) {
            log.error("环境变量 OSS_ACCESS_KEY_SECRET 未设置或为空，请参考 docs/环境变量配置指南.md 进行配置");
            throw new IllegalStateException("环境变量 OSS_ACCESS_KEY_SECRET 未设置，请配置后重启应用");
        }

        log.info("开始上传文件到阿里云OSS，bucket: {}, region: {}, 文件名: {}", bucketName, region, originalFilename);

        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String objectName = dir + "/" + UUID.randomUUID() + suffix;

        EnvironmentVariableCredentialsProvider credentialsProvider =
                CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

        ClientBuilderConfiguration configuration = new ClientBuilderConfiguration();
        configuration.setSignatureVersion(SignVersion.V4);

        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(configuration)
                .region(region)
                .build();

        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content));
            log.info("文件上传成功，objectName: {}", objectName);
        } catch (Exception e) {
            log.error("文件上传失败：{}", e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().contains("AccessDenied")) {
                throw new RuntimeException("OSS访问被拒绝，可能原因：\n" +
                        "1. AccessKey没有权限，请在RAM控制台为用户授予OSS权限\n" +
                        "2. Bucket不存在或名称错误\n" +
                        "3. Bucket ACL设置过于严格\n" +
                        "详细信息请参考 docs/环境变量配置指南.md", e);
            }
            throw e;
        } finally {
            ossClient.shutdown();
        }

        String endpointHost = endpoint.replaceFirst("^https?://", "");
        String fileUrl = "https://" + bucketName + "." + endpointHost + "/" + objectName;
        log.info("文件访问地址: {}", fileUrl);
        return fileUrl;
    }
}