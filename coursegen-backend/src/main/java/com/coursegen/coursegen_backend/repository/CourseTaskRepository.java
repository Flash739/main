package com.coursegen.coursegen_backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.coursegen.coursegen_backend.model.CourseTask;

import java.util.Optional;

public interface CourseTaskRepository extends MongoRepository<CourseTask,String> {
    Optional<CourseTask> findByTopic(String topic);

}
