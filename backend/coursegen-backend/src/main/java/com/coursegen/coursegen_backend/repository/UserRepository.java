package com.coursegen.coursegen_backend.repository;

import com.coursegen.coursegen_backend.model.CourseTask;
import com.coursegen.coursegen_backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User,String> {
    Optional<User> findByEmail(String email);
}
