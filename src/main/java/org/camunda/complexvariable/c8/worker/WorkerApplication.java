package org.camunda.complexvariable.c8.worker;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableZeebeClient
@SpringBootApplication
public class WorkerApplication {
    private final Logger logger = LoggerFactory.getLogger(WorkerApplication.class.getName());

    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);

    }
}
