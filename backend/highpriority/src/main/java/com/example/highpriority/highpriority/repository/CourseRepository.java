package com.example.highpriority.highpriority.repository;

import com.example.highpriority.highpriority.model.CourseOutline;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface CourseRepository extends MongoRepository<CourseOutline, String> {
}