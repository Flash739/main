package com.example.highpriority.highpriority;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HighpriorityApplication {

	public static void main(String[] args) {
		SpringApplication.run(HighpriorityApplication.class, args);
	}

}
