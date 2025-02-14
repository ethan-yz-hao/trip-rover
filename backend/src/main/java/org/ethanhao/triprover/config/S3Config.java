package org.ethanhao.triprover.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@Slf4j
public class S3Config {
    @Value("${aws.accessKeyId}")
    private String accessKeyId;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3.avatar-bucket-name}")
    private String avatarBucketName;

    @Value("${aws.s3.avatar-bucket-domain}")
    private String avatarBucketDomain;

    @Value("${aws.s3.default-avatar-key}")
    private String defaultAvatarKey;
    

    @Bean
    public S3Client s3Client() {
        log.info("Initializing AWS S3 client for bucket: {}", avatarBucketName);
        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretKey)))
                .region(Region.of(region))
                .build();
    }
    
    @Bean
    public String avatarBucketName() {
        return avatarBucketName;
    }

    @Bean
    public String avatarBucketDomain() {
        return avatarBucketDomain;
    }

    @Bean
    public String defaultAvatarKey() {
        return defaultAvatarKey;
    }

    @Bean
    public String defaultAvatarUrl() {
        return String.format("https://%s/%s", avatarBucketDomain, defaultAvatarKey);
    }
} 