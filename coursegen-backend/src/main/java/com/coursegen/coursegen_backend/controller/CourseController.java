package com.coursegen.coursegen_backend.controller;

import com.coursegen.coursegen_backend.model.*;
import com.coursegen.coursegen_backend.repository.CourseTaskRepository;
import com.coursegen.coursegen_backend.repository.UserRepository;
import com.coursegen.coursegen_backend.service.CourseTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    private final CourseTaskService service;


    @Autowired
    public CourseController(CourseTaskService service) {
        this.service = service;
    }

    // Create a new course generation task
    @PostMapping("/generate")
    public CourseTask createTask(@RequestBody Generatecourse generatecourse) {
              CourseTask courseTask=  service.createTask(generatecourse.getPrompt(),generatecourse.getUserid());
        System.out.println("2");
        System.out.println(String.valueOf(courseTask));
                return  courseTask;
    }

    // Get a task by ID
    @GetMapping("/polling/{id}")
    public Optional<CourseOutline> getTask(@PathVariable String id) {
       System.out.println("hello");
        System.out.println(id);

        return service.getTaskById(id);
    }
    @PostMapping("/highpriority")
    public void postsection(@RequestBody HighPriorityRequest request){
        service.puthighpriorityQueue(request);
    }

    @PostMapping("/resultpriority")
    public ResponseEntity<?> result(@RequestBody HighPriorityRequest request){
        return ResponseEntity.ok(service.resulthighpriorityQueue(request));
    }

    // (Optional) Update an existing task if needed
    @PutMapping("/{id}")
    public CourseTask updateTask(@PathVariable String id, @RequestBody CourseTask updatedTask) {
        updatedTask.setId(id);
        return service.updateTask(updatedTask);
    }

    @GetMapping("/{userId}/courses")
    public ResponseEntity<?> getUserCourses(@PathVariable String userId) {

        List<CourseTask> courses=service.getUserCourse(userId);
        return ResponseEntity.ok(courses);
    }
}