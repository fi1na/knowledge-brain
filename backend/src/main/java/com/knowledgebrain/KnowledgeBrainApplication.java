package com.knowledgebrain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class KnowledgeBrainApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeBrainApplication.class, args);
    }
}
