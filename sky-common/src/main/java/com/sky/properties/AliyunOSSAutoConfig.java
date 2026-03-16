package com.sky.properties;


import com.sky.utils.AliyunOSSOperator;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AliOssProperties.class)
public class AliyunOSSAutoConfig {


    @Bean
    public AliyunOSSOperator aliyOssProperties(AliOssProperties aliOssProperties) {
        return new AliyunOSSOperator(aliOssProperties);
    }

}


