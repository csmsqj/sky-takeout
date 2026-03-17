package  com.sky.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.sky.properties.AliOssProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
public class AliyunOSSOperator {

    private AliOssProperties aliyunOSSProperties;

    public AliyunOSSOperator(AliOssProperties aliyunOSSProperties){
        this.aliyunOSSProperties = aliyunOSSProperties;
    }

//这里可以有参构造注入，或者直接@Autowired注入AliOssProperties对象，这样就可以在这个类中使用AliOssProperties对象了
    public String upload(byte[] content, String originalFilename) throws Exception {
        String endpoint = aliyunOSSProperties.getEndpoint();
        String region = aliyunOSSProperties.getRegionId();
        String bucketName = aliyunOSSProperties.getBucketName();

        // 获取访问密钥：优先使用配置文件中的值，如果没有则从环境变量获取
        String accessKeyId;
        String secretAccessKey;

        if (aliyunOSSProperties.getAccessKey() != null && !aliyunOSSProperties.getAccessKey().isEmpty() &&
            aliyunOSSProperties.getSecretKey() != null && !aliyunOSSProperties.getSecretKey().isEmpty()) {
            // 从配置文件获取（配置文件中应该使用 ${} 引用环境变量）
            accessKeyId = aliyunOSSProperties.getAccessKey();
            secretAccessKey = aliyunOSSProperties.getSecretKey();
            log.info("使用配置文件中的 OSS 凭证");
        } else {
            // 从环境变量中获取访问凭证
            EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
            accessKeyId = credentialsProvider.getCredentials().getAccessKeyId();
            secretAccessKey = credentialsProvider.getCredentials().getSecretAccessKey();
            log.info("使用环境变量中的 OSS 凭证");
        }

        // 填写Object完整路径，例如202406/1.png。Object完整路径中不能包含Bucket名称。
        //获取当前系统日期的字符串,格式为 yyyy/MM
        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        //生成一个新的不重复的文件名
        String newFileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = dir + "/" + newFileName;

        // 创建OSSClient实例 (使用3.10.2版本的API)
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, secretAccessKey);

        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content));
        } finally {
            ossClient.shutdown();
        }

        return endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + objectName;
    }

}
