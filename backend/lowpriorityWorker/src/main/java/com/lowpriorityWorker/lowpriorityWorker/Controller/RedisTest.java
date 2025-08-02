package com.lowpriorityWorker.lowpriorityWorker.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisTest {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/test-redis")
    public String testRedis() {
        //System.out.println("jkrbfkjrbg");
        redisTemplate.opsForValue().set("test:ping", "pong");
        return redisTemplate.opsForValue().get("test:ping");
    }
}
