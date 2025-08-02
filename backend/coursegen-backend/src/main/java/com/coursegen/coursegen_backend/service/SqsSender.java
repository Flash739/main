package com.coursegen.coursegen_backend.service;


import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
    public class SqsSender {

        private final SqsClient sqsClient;
        private final String queueUrl;

        public SqsSender(SqsClient sqsClient) {
            this.sqsClient = sqsClient;
            this.queueUrl = "https://sqs.ap-south-1.amazonaws.com/029654768811/course-generation";
        }

        public void sendMessage(String messageBody) {
            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build();
            sqsClient.sendMessage(request);
        }
    }
