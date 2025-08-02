package com.coursegenerationworker.CourseGenerationWorker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CourseGenerationWorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CourseGenerationWorkerApplication.class, args);
	}

}
