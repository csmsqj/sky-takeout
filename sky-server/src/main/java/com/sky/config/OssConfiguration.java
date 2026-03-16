package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class OssConfiguration {
    //有参构造方法，Spring会自动调用这个构造方法，并且会自动注入AliOssProperties对象
    //也可以自定义一个参数类，来接收AliOssProperties对象，这样就可以在这个类中使用AliOssProperties对象了
    @Bean
    public AliOssUtil aliOssConfig(AliOssProperties aliOssProperties){
        log.info("创建AliOssConfig对象，参数：{}", aliOssProperties);
        return new AliOssUtil(aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName());
    }


}
