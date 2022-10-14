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
        log.info("{}: Got the list of size {} task[0]={}", methodName, rankedTaskList.size(), rankedTaskList.get(0).getEnqueueTime());
        return ResponseEntity.status(HttpStatus.OK).body(rankedTaskList);
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

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(LoremIpsumApplication.class);
    }
}
