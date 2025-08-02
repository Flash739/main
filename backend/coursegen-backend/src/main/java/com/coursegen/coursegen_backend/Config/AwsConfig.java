package com.coursegen.coursegen_backend.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class AwsConfig {

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(Region.of("ap-south-1")) // Change to your region
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        "AKIAQNZ4P3SVVYECXSUH",   // 🔐 Replace with your access key
                                        "GlKsjl9TxwtoIg08a6Z4O7wrnWAjBW/LcSzj5UsO"    // 🔐 Replace with your secret key
                                )
                        )
                )
                .build();
    }
}
