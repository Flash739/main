package com.example.highpriority.highpriority.Service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.example.highpriority.highpriority.Client.LlmClient;
import com.example.highpriority.highpriority.model.CourseOutline;
import com.example.highpriority.highpriority.repository.CourseRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HighPriorityWorkerService {

    @Autowired
    private AmazonSQS sqsClient;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LlmClient llmClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String queueUrl = "https://sqs.ap-south-1.amazonaws.com/029654768811/HighPriorityQueue";

    @Scheduled(fixedDelay = 5000)
    public void pollQueue() {
        List<Message> messages = sqsClient.receiveMessage(new ReceiveMessageRequest()
                        .withQueueUrl(queueUrl)
                        .withMaxNumberOfMessages(1)
                        .withWaitTimeSeconds(10))
                .getMessages();

        for (Message message : messages) {
            try {
                JsonNode root = new ObjectMapper().readTree(message.getBody());
                String taskId = root.path("taskId").asText();
                final String sectionTitle = root.path("sectionTitle").asText();
                final String subsectionTitle = root.has("subsectionTitle") ? root.path("subsectionTitle").asText() : null;

                // Redis key
               String redisKey = null;
                if(subsectionTitle == null || subsectionTitle.trim().isEmpty() || subsectionTitle.isBlank() || subsectionTitle.equalsIgnoreCase("null")){
                    redisKey = "section:" + taskId + ":" + sectionTitle;
                    //redisKey = "subsection:" + taskId + ":" + sectionTitle + ":" + subsectionTitle;
               }else{
                    redisKey = "subsection:" + taskId + ":" + sectionTitle + ":" + subsectionTitle;
                }

                System.out.println(redisKey);


                if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
                    System.out.println("Found in Redis: " + redisKey);
                    sqsClient.deleteMessage(queueUrl, message.getReceiptHandle());
                    continue;
                }

                Optional<CourseOutline> optionalCourse = courseRepository.findById(taskId);
                if (optionalCourse.isEmpty()) {
                    System.err.println("Course not found for taskId: " + taskId);
                    continue;
                }

                CourseOutline course = optionalCourse.get();
                CourseOutline.Section section = course.findSection(sectionTitle);

                if (section == null) {
                    System.err.println("Section not found: " + sectionTitle);
                    continue;
                }
                System.out.println(sectionTitle+","+2+","+subsectionTitle);

                if (subsectionTitle == null || subsectionTitle.trim().isEmpty() || subsectionTitle.isBlank() || subsectionTitle.equalsIgnoreCase("null")) {
                    // Section-level detail generation
                    System.out.println(sectionTitle+","+3);

                    if (section.getDescription() == null || section.getDescription().isBlank()) {
                        section.setDescription(llmClient.generateSectionDescription(course.getTopic(), sectionTitle));
                    }

                    if (section.getVideos() == null || section.getVideos().isEmpty()) {
                        section.setVideos(llmClient.generateSectionVideos(course.getTopic(), sectionTitle));
                    }

                    if (section.getSubsections() == null || section.getSubsections().isEmpty()) {
                        List<String> titles = llmClient.getSubsectionTitles(course.getTopic(), sectionTitle);
                        section.setSubsections(titles.stream().map(title -> {
                            CourseOutline.Subsection s = new CourseOutline.Subsection();
                            s.setTitle(title);
                            return s;
                        }).toList());
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    String json = mapper.writeValueAsString(section);

                    System.out.println(redisKey);
                    System.out.println(json+","+1);


                    redisTemplate.opsForValue().set(redisKey, json);

                } else {
                    // Subsection-level detail generation
                    CourseOutline.Subsection subsection = section.findSubsection(subsectionTitle);

                    System.out.println(subsectionTitle+","+2+","+sectionTitle);


                    if (subsection == null) {
                        subsection = new CourseOutline.Subsection();
                        section.getSubsections().add(subsection);
                    }
                    subsection.setTitle(subsectionTitle);

                    if (subsection.getDescription() == null || subsection.getDescription().isBlank()) {
                        subsection.setDescription(llmClient.generateSubsectionContent(course.getTopic(), sectionTitle, subsectionTitle));
                    }

                    if (subsection.getVideos() == null || subsection.getVideos().isEmpty()) {
                        subsection.setVideos(llmClient.generateSubsectionVideos(course.getTopic(), sectionTitle, subsectionTitle));
                    }

                    if (subsection.getQuiz() == null || subsection.getQuiz().isEmpty()) {
                        subsection.setQuiz(llmClient.generateSubsectionQuiz(course.getTopic(), sectionTitle, subsectionTitle));
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    String json = mapper.writeValueAsString(subsection);

                    System.out.println(redisKey);
                    System.out.println(json);

                    redisTemplate.opsForValue().set(redisKey, json);
                }

                courseRepository.save(course);
                sqsClient.deleteMessage(queueUrl, message.getReceiptHandle());

            } catch (Exception e) {
                System.err.println("Error processing high-priority message: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}

