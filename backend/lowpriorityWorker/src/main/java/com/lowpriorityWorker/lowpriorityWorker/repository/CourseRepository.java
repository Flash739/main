package com.lowpriorityWorker.lowpriorityWorker.repository;

import com.lowpriorityWorker.lowpriorityWorker.model.CourseOutline;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface CourseRepository extends MongoRepository<CourseOutline, String> {
}