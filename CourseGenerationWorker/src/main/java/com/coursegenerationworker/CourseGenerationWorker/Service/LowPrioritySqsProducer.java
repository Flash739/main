package com.coursegenerationworker.CourseGenerationWorker.Service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LowPrioritySqsProducer {

    public static class DetailSqssend {
        public String taskId;
        public String topic;

        public DetailSqssend() {
        }

        public DetailSqssend(String taskId, String topic) {
            this.taskId = taskId;
            this.topic = topic;
        }
    }
    @Autowired
    private AmazonSQS amazonSQS;

    private String lowPriorityQueueUrl = "https://sqs.ap-south-1.amazonaws.com/029654768811/DetailCoursegeneration";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendDetailsTask(String taskId,String topic) {
        try {
            DetailSqssend detailSqssend=new DetailSqssend();
            detailSqssend.taskId=taskId;
            detailSqssend.topic=topic;

            String jsonMessage = objectMapper.writeValueAsString(detailSqssend);

            amazonSQS.sendMessage(lowPriorityQueueUrl, jsonMessage);
            System.out.println("✅ Sent to low-priority SQS: " + jsonMessage);
        } catch (Exception e) {
            System.err.println("❌ Failed to send to low-priority SQS");
            e.printStackTrace();
        }
    }
}

