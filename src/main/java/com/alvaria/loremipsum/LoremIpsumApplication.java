package com.alvaria.loremipsum;

import com.alvaria.loremipsum.queue.TaskPriorityQueue;
import lombok.extern.slf4j.Slf4j;
import org.json.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@SpringBootApplication
@RestController
@Slf4j
public class LoremIpsumApplication extends SpringBootServletInitializer {
    TaskPriorityQueue queue;

    public static void main(String[] args) {
        SpringApplication.run(LoremIpsumApplication.class, args);
        log.info("Lorem ipsum dolor sit amet - APPLICATION STARTED");
    }

    @Autowired
    public void setQueue(TaskPriorityQueue queue) {
        this.queue = queue;
    }

    @GetMapping("/hello")
    public String sayHello(@RequestParam(value = "myName", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }

    @PostMapping(value = "/newtask")
    public HttpStatus newTask(@RequestBody String body) {
        String methodName = "newTask";
        log.info("Body: {}", body);
        JSONObject jsonBody = new JSONObject(body);
        try {
            long id = jsonBody.getLong("id");
            long enqueueTime = jsonBody.getLong("enqueueTime");
            TaskPriorityQueue.Status status = queue.addNewTask(id,enqueueTime);

            if (status == TaskPriorityQueue.Status.S_OK) {
                log.info("{}: new task added to the queue", methodName);
                return HttpStatus.OK;
            } else  {
                log.info("{}: Failed to add new task: {}", methodName, status);
                return HttpStatus.BAD_REQUEST;
            }
        } catch (JSONException ex) {
            log.error("{}: failed to parse the JSON string [{}]", methodName, body);
            ex.printStackTrace();
            return HttpStatus.BAD_REQUEST;
        }
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(LoremIpsumApplication.class);
    }
}
