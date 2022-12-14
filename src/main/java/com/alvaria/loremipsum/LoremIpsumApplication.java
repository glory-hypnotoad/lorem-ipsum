package com.alvaria.loremipsum;

import com.alvaria.loremipsum.queue.TaskPriorityQueue;
import com.alvaria.loremipsum.tasks.RankedTask;
import lombok.extern.slf4j.Slf4j;
import org.json.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping(value = "/newtask")
    public @ResponseBody ResponseEntity<?> newTask(@RequestBody String body) {
        String methodName = "newTask";
        log.info("Body: {}", body);
        JSONObject jsonBody = new JSONObject(body);
        try {
            long id = jsonBody.getLong("id");
            long enqueueTime = jsonBody.getLong("enqueueTime");
            TaskPriorityQueue.Status status = queue.addNewTask(id,enqueueTime);

            if (status == TaskPriorityQueue.Status.S_OK) {
                log.info("{}: new task added to the queue", methodName);
                return ResponseEntity.status(HttpStatus.OK).build();
            } else if (status == TaskPriorityQueue.Status.E_QUEUE_FULL) {
                log.info("{}: Queue is full; status: {}", methodName, status);
                JSONObject obj = new JSONObject();
                obj.put("status", status);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(obj.toString());
            } else {
                log.info("{}: Failed to add new task: {}", methodName, status);
                JSONObject obj = new JSONObject();
                obj.put("status", status);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(obj.toString());
            }
        } catch (JSONException ex) {
            log.error("{}: failed to parse the JSON string [{}]", methodName, body);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/poll")
    public @ResponseBody ResponseEntity<?> poll() {
        String methodName = "poll";
        log.info("{}: Polling the queue", methodName);
        RankedTask task = queue.poll();
        if (task != null) {
            return ResponseEntity.status(HttpStatus.OK).body(task);
        } else {
            log.info("{}: Task not found (empty tree)", methodName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/listIds")
    public @ResponseBody ResponseEntity<?> listIds() {
        String methodName = "listIds";
        log.info("{}: Getting the list of tasks in the queue", methodName);
        List<RankedTask> rankedTaskList = queue.getRankedTaskList();
        if (rankedTaskList != null) {
            log.info("{}: Got the list of size {}", methodName, rankedTaskList.size());
            return ResponseEntity.status(HttpStatus.OK).body(rankedTaskList);
        } else {
            log.info("{}: Queue is empty", methodName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/position/{id}")
    public @ResponseBody ResponseEntity<?> getPosition(@PathVariable Long id) {
        String methodName = "getPosition";
        log.info("{}: Getting the position of task in the queue: {}", methodName, id);
        Integer pos =  queue.getTaskPosition(id);
        if (pos < 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            JSONObject obj = new JSONObject();
            obj.put("position", pos);
            return ResponseEntity.status(HttpStatus.OK).body(obj.toString());
        }
    }

    @DeleteMapping("/task/{id}")
    public @ResponseBody ResponseEntity<?> deleteTask(@PathVariable Long id) {
        String methodName = "deleteTask";
        log.info("{}: Deleting the task from queue: {}", methodName, id);
        TaskPriorityQueue.Status status = queue.deleteTask(id);
        if (status == TaskPriorityQueue.Status.S_OK) {
            log.info("{}: Task {} deleted", methodName, id);
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            log.info("{}: Task {} not found", methodName, id);
            JSONObject obj = new JSONObject();
            obj.put("status", status);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(obj.toString());
        }
    }

    @GetMapping("/ewt")
    public @ResponseBody ResponseEntity<?> getEWT() {
        String methodName = "getEWT";
        log.info("{}: Getting the average waiting time in the queue", methodName);
        Long ewt = queue.getExpectedWaitTime();
        JSONObject obj = new JSONObject();
        obj.put("EWT", ewt);
        return ResponseEntity.status(HttpStatus.OK).body(obj.toString());
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(LoremIpsumApplication.class);
    }
}
