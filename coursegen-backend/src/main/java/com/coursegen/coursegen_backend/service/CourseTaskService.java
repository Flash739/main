package com.coursegen.coursegen_backend.service;

import com.coursegen.coursegen_backend.model.*;
import com.coursegen.coursegen_backend.repository.CourseOutlineRepository;
import com.coursegen.coursegen_backend.repository.CourseTaskRepository;
import com.coursegen.coursegen_backend.repository.UserRepository;
import com.coursegen.coursegen_backend.service.Nlu.NluService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CourseTaskService {

    @Autowired
    private CourseTaskRepository repository;

    @Autowired
    private CourseOutlineRepository outlineRepository;

    @Autowired
    private NluService nluservice;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SqsSender sqsSender;

    @Autowired
    private HighPrioritySender highPrioritySender;

    @Autowired
    private RedisTemplate redisTemplate;


    public CourseTask createTask(String prompt,String userId){



        CourseTask courseTask = new CourseTask();


        courseTask.setStatus("Pending");
        courseTask.setUserPrompt(prompt);

        String intent = nluservice.extractIntent(prompt);
        String topic = nluservice.extractTopic(prompt);

        if(topic.charAt(0) !='"'){
            topic='"'+topic+'"';
        }


        System.out.println(topic+","+intent);

        Optional<CourseTask> existing = repository.findByTopic(topic);

        if (existing.isPresent()) {
            // If course already exists, return its existing ID (reuse taskId or generate mapping logic)
            return existing.get(); // or store in Redis for fast access
        }

        courseTask.setIntent(intent);
        courseTask.setTopic(topic);

        String formatString = nluservice.formatPrompt(intent, topic, prompt);
        courseTask.setFormattedPrompt(formatString);

        // Save to DB
        CourseTask savedTask = repository.save(courseTask);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> payload = Map.of(
                "taskId", savedTask.getId(),
                "topic", savedTask.getTopic()
        );

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.getCreatedCourseIds().add(savedTask.getId());
        userRepository.save(user);
        try {
            String messageBody = mapper.writeValueAsString(payload);
            System.out.println(messageBody);
            sqsSender.sendMessage(messageBody);

            return savedTask;

        }catch (Exception exception){
            exception.printStackTrace();
        }
        return null;
    }
    public Optional<CourseOutline> getTaskById(String id) {

        return outlineRepository.findById(id);
    }
    public void puthighpriorityQueue(HighPriorityRequest highPriorityRequest){
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", highPriorityRequest.getTaskId());
        payload.put("sectionTitle", highPriorityRequest.getSectionTitle());
        // To send without subsectionTitle, either don't add it or set to null
        payload.put("subsectionTitle", (highPriorityRequest.getSubsectionTitle()!=null?highPriorityRequest.getSubsectionTitle():null)); // or omit this line

        System.out.println(highPriorityRequest.getTaskId());
        System.out.println(highPriorityRequest.getSectionTitle());
        System.out.println(highPriorityRequest.getSubsectionTitle());




        try {
            String messageBody = objectMapper.writeValueAsString(payload);
            highPrioritySender.sendMessage(messageBody);


        }catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    public HighPriorityResponse resulthighpriorityQueue(HighPriorityRequest highPriorityRequest) {

        String redisKey;
        String subsectionTitle = highPriorityRequest.getSubsectionTitle();
        String sectionTitle = highPriorityRequest.getSectionTitle();
        String taskId = highPriorityRequest.getTaskId();

        if (subsectionTitle != null && !subsectionTitle.isEmpty()) {
            redisKey = "subsection:" + taskId + ":" + sectionTitle + ":" + subsectionTitle;
        } else {
            redisKey = "section:" + taskId + ":" + sectionTitle;
        }
        System.out.println(redisKey+"j"+2);

        String jsonString = (String) redisTemplate.opsForValue().get(redisKey);
        System.out.println(jsonString);
        if(jsonString ==null){
            return new HighPriorityResponse(null, null);
        }

// Optional: Convert back to object
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(redisKey);
        System.out.println(jsonString);
        try {
            if (subsectionTitle == null) {
                System.out.println("here3");

                CourseOutline.Section section = objectMapper.readValue(jsonString, CourseOutline.Section.class);
                System.out.println("here4");
                return new HighPriorityResponse(section, null);
            } else {

                CourseOutline.Subsection subsection = objectMapper.readValue(jsonString, CourseOutline.Subsection.class);
                System.out.println("here3");
                return new HighPriorityResponse(null, subsection);
            }
        }catch(Exception exception){
            exception.printStackTrace();
        }
        return new HighPriorityResponse(null, null);
    }

    public CourseTask updateTask(CourseTask task) {
        return repository.save(task);
    }

    public List<CourseTask> getUserCourse(String userid){
        Optional<User> userOpt = userRepository.findById(userid);
        if (userOpt.isEmpty()){
            List<CourseTask> courses =new ArrayList<>();
            return courses;
        }

        List<String> courseIds = userOpt.get().getCreatedCourseIds();
        List<CourseTask> courses = repository.findAllById(courseIds);
    return courses;
    }
}
