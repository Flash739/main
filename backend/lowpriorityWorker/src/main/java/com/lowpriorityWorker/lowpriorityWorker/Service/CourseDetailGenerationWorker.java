package com.lowpriorityWorker.lowpriorityWorker.Service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowpriorityWorker.lowpriorityWorker.Client.LlmClient;
import com.lowpriorityWorker.lowpriorityWorker.model.CourseOutline;
import com.lowpriorityWorker.lowpriorityWorker.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CourseDetailGenerationWorker {

    @Autowired
    private AmazonSQS sqsClient;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LlmClient llmClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    private final String queueUrl = "https://sqs.ap-south-1.amazonaws.com/029654768811/DetailCoursegeneration";

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
                String topic = root.path("topic").asText();

                Optional<CourseOutline> optionalCourse = courseRepository.findById(taskId);
                if (optionalCourse.isEmpty()) {
                    System.err.println("Course not found for taskId: " + taskId);
                    continue;
                }

                CourseOutline course = optionalCourse.get();
                boolean changed = false;

                for (CourseOutline.Section section : course.getSections()) {

                    // 1. Section Description

                    String redisKey = "section:" + course.getId() + ":" + section.getTitle();
                    boolean sectionComplete = true;


                    if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
                        //System.out.println("Found in Redis: " + redisKey);

                        String json = (String) redisTemplate.opsForValue().get(redisKey);
                        System.out.println("Loaded from Redis: " + redisKey+","+json);

                        ObjectMapper mapper = new ObjectMapper();
                        CourseOutline.Section cachedSub = mapper.readValue(json, CourseOutline.Section.class);

                        section.setDescription(cachedSub.getDescription());
                        section.setVideos(cachedSub.getVideos());
                        section.setSubsections(cachedSub.getSubsections());

                        //sqsClient.deleteMessage(queueUrl, message.getReceiptHandle())
                    }else {

                        CourseOutline.Section sectionredis = new CourseOutline.Section();


                        if (section.getDescription() == null || section.getDescription().isBlank()) {
                            String desc = llmClient.generateSectionDescription(topic, section.getTitle());
                            section.setDescription(desc);
                            sectionredis.setDescription(desc);
                            changed = true;
                        }

                        // 2. Section-Level Video
                        if (section.getVideos() == null || section.getVideos().isEmpty()) {
                            List<CourseOutline.Video> videos = llmClient.generateSectionVideos(topic, section.getTitle());
                            section.setVideos(videos);
                            sectionredis.setVideos(videos);
                            changed = true;
                        }

                        // 3. Subsection Titles
                        if (section.getSubsections() == null || section.getSubsections().isEmpty()) {
                            List<String> subsectionTitles = llmClient.getSubsectionTitles(topic, section.getTitle());
                            List<CourseOutline.Subsection> subsections = new ArrayList<>();
                            for (String title : subsectionTitles) {
                                CourseOutline.Subsection s = new CourseOutline.Subsection();
                                s.setTitle(title);
                                subsections.add(s);
                            }
                            section.setSubsections(subsections);
                            sectionredis.setSubsections(subsections);
                            changed = true;
                        }
                        ObjectMapper mapper = new ObjectMapper();
                        String json = mapper.writeValueAsString(section);

                        redisTemplate.opsForValue().set(redisKey, json);

                    }


                    // 4. Subsection Details
                    System.out.println(section.getSubsections().size());

                    for (CourseOutline.Subsection subsection : section.getSubsections()) {
                        String subKey = "subsection:" + course.getId() + ":" + section.getTitle() + ":" + subsection.getTitle();

                        if (Boolean.TRUE.equals(redisTemplate.hasKey(subKey))) {
                            //System.out.println("Found in Redis: " + redisKey);

                            System.out.println("Loaded from Redis: " + subKey);
                            String json = (String) redisTemplate.opsForValue().get(subKey);
                            ObjectMapper mapper = new ObjectMapper();
                            CourseOutline.Subsection cachedSub = mapper.readValue(json, CourseOutline.Subsection.class);

                            subsection.setDescription(cachedSub.getDescription());
                            subsection.setVideos(cachedSub.getVideos());
                            subsection.setQuiz(cachedSub.getQuiz());

                            //sqsClient.deleteMessage(queueUrl, message.getReceiptHandle())
                        } else {


                            CourseOutline.Subsection subsection1 = new CourseOutline.Subsection();
                            subsection1.setTitle(section.getTitle());


                            if (subsection.getDescription() == null || subsection.getDescription().isBlank()) {
                                String content = llmClient.generateSubsectionContent(topic, section.getTitle(), subsection.getTitle());
                                subsection.setDescription(content);
                                subsection1.setDescription(content);
                                changed = true;
                            }

                            if (subsection.getQuiz() == null || subsection.getQuiz().isEmpty()) {
                                List<CourseOutline.Quiz> quiz = llmClient.generateSubsectionQuiz(topic, section.getTitle(), subsection.getTitle());
                                subsection.setQuiz(quiz);
                                subsection1.setQuiz(quiz);
                                changed = true;
                            }

                            if (subsection.getVideos() == null || subsection.getVideos().isEmpty()) {
                                List<CourseOutline.Video> videos = llmClient.generateSubsectionVideos(topic, section.getTitle(), subsection.getTitle());
                                subsection.setVideos(videos);
                                subsection1.setVideos(videos);
                                changed = true;
                            }
                            ObjectMapper mapper = new ObjectMapper();
                            String jsonsubsection = mapper.writeValueAsString(subsection1);
                            redisTemplate.opsForValue().set(subKey, jsonsubsection);
                        }
                    }
                }
/*
                if (changed) {
                    courseRepository.save(course);
                }
*/


                if (isCourseFullyPopulated(course)) {
                    course.setStatus("COMPLETED");
                    courseRepository.save(course);
                    sqsClient.deleteMessage(queueUrl, message.getReceiptHandle());
                    System.out.println("Completed course: " + taskId);
                } else {
                    System.out.println("Partial update done. More fields pending for taskId: " + taskId);
                    courseRepository.save(course);
                }
            } catch (Exception e) {
                System.err.println("Failed to process message: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean isCourseFullyPopulated(CourseOutline course) {
        if (course.getSections() == null || course.getSections().isEmpty()) return false;

        for (CourseOutline.Section section : course.getSections()) {
            if (section.getDescription() == null || section.getDescription().isBlank()) return false;
            if (section.getVideos() == null || section.getVideos().isEmpty()) return false;
            if (section.getSubsections() == null || section.getSubsections().isEmpty()) return false;

            for (CourseOutline.Subsection subsection : section.getSubsections()) {
                if (subsection.getDescription() == null || subsection.getDescription().isBlank()) return false;
                if (subsection.getQuiz() == null || subsection.getQuiz().isEmpty()) return false;
                if (subsection.getVideos() == null || subsection.getVideos().isEmpty()) return false;
            }
        }

        return true;
    }
}
