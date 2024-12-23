package org.ethanhao.triprover.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class AWSConfig {
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

    @Bean
    public AmazonS3 amazonS3() {
        log.info("Initializing AWS S3 client for bucket: {}", avatarBucketName);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(accessKeyId, secretKey)))
                .withRegion(region)
                .build();
    }

    @Bean
    public String avatarBucketDomain() {
        return avatarBucketDomain;
    }
} 