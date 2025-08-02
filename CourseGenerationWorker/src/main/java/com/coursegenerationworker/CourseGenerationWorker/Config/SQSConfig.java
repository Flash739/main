package com.coursegenerationworker.CourseGenerationWorker.Config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class SQSConfig {


    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public AmazonSQS amazonSQS() {
        return AmazonSQSClientBuilder.standard()
                .withRegion("ap-south-1")
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(
                                "AKIAQNZ4P3SVVYECXSUH", // 🔐 Replace safely
                                "GlKsjl9TxwtoIg08a6Z4O7wrnWAjBW/LcSzj5UsO"
                        )
                ))
                .build();
    }
}
