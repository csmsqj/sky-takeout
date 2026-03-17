package com.sky.utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
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
        String accessKey = aliOssProperties.getAccessKey();
        String secretKey = aliOssProperties.getSecretKey();

        // 验证必需的配置
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalStateException("OSS endpoint未配置，请检查application-dev.yml中的sky.alioss.endpoint配置");
        }
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalStateException("OSS bucket-name未配置，请检查application-dev.yml中的sky.alioss.bucket-name配置");
        }
        if (region == null || region.trim().isEmpty()) {
            throw new IllegalStateException("OSS region未配置，请检查application-dev.yml中的sky.alioss.region配置");
        }

        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String objectName = dir + "/" + UUID.randomUUID() + suffix;

        // 创建凭证提供者
        // 优先使用配置文件中的accessKey和secretKey
        // 如果配置文件中没有，则尝试从环境变量读取
        CredentialsProvider credentialsProvider;
        if (accessKey != null && !accessKey.trim().isEmpty()
                && secretKey != null && !secretKey.trim().isEmpty()) {
            log.info("使用配置文件中的OSS凭证");
            credentialsProvider = new DefaultCredentialProvider(accessKey, secretKey);
        } else {
            log.info("配置文件中未找到OSS凭证，尝试从环境变量读取");
            try {
                credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
            } catch (Exception e) {
                throw new IllegalStateException(
                    "OSS凭证未配置！请设置以下环境变量：\n" +
                    "  - OSS_ACCESS_KEY_ID (或 ALIBABA_CLOUD_ACCESS_KEY_ID)\n" +
                    "  - OSS_ACCESS_KEY_SECRET (或 ALIBABA_CLOUD_ACCESS_KEY_SECRET)\n" +
                    "或者在application-dev.yml中配置：\n" +
                    "  sky.alioss.access-key\n" +
                    "  sky.alioss.secret-key\n" +
                    "详细配置方法请参考 ENVIRONMENT_SETUP.md 文件", e);
            }
        }

        ClientBuilderConfiguration configuration = new ClientBuilderConfiguration();
        configuration.setSignatureVersion(SignVersion.V4);

        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(configuration)
                .region(region)
                .build();

        try {
            log.info("上传文件到OSS: bucket={}, objectName={}", bucketName, objectName);
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content));
            log.info("文件上传成功");
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传到OSS失败，请检查：\n" +
                    "1. OSS凭证是否正确\n" +
                    "2. Bucket名称是否正确: " + bucketName + "\n" +
                    "3. Bucket的ACL权限是否正确配置\n" +
                    "4. 网络连接是否正常\n" +
                    "详细错误: " + e.getMessage(), e);
        } finally {
            ossClient.shutdown();
        }

        String endpointHost = endpoint.replaceFirst("^https?://", "");
        return "https://" + bucketName + "." + endpointHost + "/" + objectName;
    }
}