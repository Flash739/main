package com.coursegenerationworker.CourseGenerationWorker.Service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.coursegenerationworker.CourseGenerationWorker.client.OpenAiClient;
import com.coursegenerationworker.CourseGenerationWorker.model.CourseOutline;
import com.coursegenerationworker.CourseGenerationWorker.repository.CourseRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseOutlineWorkerService {

    private String queueUrl="https://sqs.ap-south-1.amazonaws.com/029654768811/course-generation";

    private final AmazonSQS sqs;
    private final CourseRepository courseRepository;
    private final OpenAiClient openAiClient;
    @Autowired
    private final LowPrioritySqsProducer lowPrioritySqsProducer;


    private final ObjectMapper mapper = new ObjectMapper();

    @Scheduled(fixedDelay = 5000)
    public void pollQueue() {
        List<Message> messages = sqs.receiveMessage(new ReceiveMessageRequest(queueUrl)
                        .withMaxNumberOfMessages(1)
                        .withWaitTimeSeconds(10))
                .getMessages();

        System.out.println("here"+","+messages.size());

        for (Message message : messages) {
            System.out.println("herer 1 message");
            try {
                JsonNode json = mapper.readTree(message.getBody());
                String taskId = json.get("taskId").asText();
                String topic = json.get("topic").asText();

                String prompt = buildPrompt(topic);

                String result = openAiClient.getCourseOutlineFromLLM(prompt);
                System.out.println("here");

                CourseOutline outline = mapper.readValue(result, CourseOutline.class);
                outline.setId(taskId);
                outline.setTopic(topic);
                courseRepository.save(outline);

                sqs.deleteMessage(new DeleteMessageRequest(queueUrl, message.getReceiptHandle()));

                lowPrioritySqsProducer.sendDetailsTask(taskId,topic);

                System.out.println("✅ Outline saved for taskId = " + taskId);

            } catch (Exception e) {
                System.err.println("❌ Failed to process message: " + e.getMessage());
            }
        }
    }
    private String buildPrompt(String topic) {
        return """
    Generate a course outline in JSON format for the topic: "%s". The JSON should follow this structure:

    {
      "sections": [
        {
          "title": "Section Title",
          "description": "Brief summary of the section",
          "subsections": [],
          "quizzes": [],
          "videos": []
        }
      ]
    }

    Ensure more then 3 sections and I want detail outline so that reader can properly understand the course . Each section should only include title of sections. Leave subsections,quizzes,description and videos as empty arrays.

    Output only valid JSON, without any explanation or extra text.
    """.formatted(topic);
    }
}