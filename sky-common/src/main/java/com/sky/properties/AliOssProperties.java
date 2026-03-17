package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


//配置属性类，读取以sky.alioss开头的属性用来赋值，再给AliOssUtil使用
@ConfigurationProperties(prefix = "sky.alioss")
@Data
public class AliOssProperties {
    private String endpoint;
    private String bucketName;
    private String regionId;
    private String accessKey;  // 阿里云访问密钥ID，建议通过环境变量配置
    private String secretKey;  // 阿里云访问密钥Secret，建议通过环境变量配置
}
