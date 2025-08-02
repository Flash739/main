package com.coursegen.coursegen_backend.repository;

import com.coursegen.coursegen_backend.model.CourseOutline;
import com.coursegen.coursegen_backend.model.CourseTask;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CourseOutlineRepository extends MongoRepository<CourseOutline, String> {
}
