package com.coursegenerationworker.CourseGenerationWorker.repository;

import com.coursegenerationworker.CourseGenerationWorker.model.CourseOutline;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface CourseRepository extends MongoRepository<CourseOutline, String> {
}