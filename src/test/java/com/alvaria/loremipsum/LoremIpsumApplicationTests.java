package com.alvaria.loremipsum;

import com.alvaria.loremipsum.tasks.RankedTask;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
class LoremIpsumApplicationTests {

    LoremIpsumApplication application;

    @Autowired
    void setApplication(LoremIpsumApplication application) {
        this.application = application;
    }

    @Test
    public void testMultipleTasksRankingAndEWT() throws JSONException {

        insertTasksOfAllClasses();

        // "Old" Management Override ID
        ResponseEntity<?>  response = application.poll();
        RankedTask responseTask = (RankedTask) response.getBody();
        assertEquals(30L, responseTask.getId());

        // "New" Management Override ID
        response = application.poll();
        responseTask = (RankedTask) response.getBody();
        assertEquals(15L, responseTask.getId());

        // "Old" VIP ID; RANK == 2119
        response = application.poll();
        responseTask = (RankedTask) response.getBody();
        assertEquals(20L, responseTask.getId());

        // "Old" Priority ID; RANK == 1059
        response = application.poll();
        responseTask = (RankedTask) response.getBody();
        assertEquals(9L, responseTask.getId());

        // "New" VIP ID; RANK == 921
        response = application.poll();
        responseTask = (RankedTask) response.getBody();
        assertEquals(25L, responseTask.getId());

        // "New" Priority ID; RANK == 460
        response = application.poll();
        responseTask = (RankedTask) response.getBody();
        assertEquals(3L, responseTask.getId());

        // "Old" Normal ID; RANK == 200
        response = application.poll();
        responseTask = (RankedTask) response.getBody();
        assertEquals(7L, responseTask.getId());

        // "New" Normal ID; RANK == 100
        response = application.poll();
        responseTask = (RankedTask) response.getBody();
        assertEquals(11L, responseTask.getId());

        // Poll the empty queue
        response = application.poll();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testInsertSameIDMultipleTimes() throws JSONException {
        long currentTime = Instant.now().getEpochSecond();

        // "New" Priority ID; RANK == 200
        JSONObject task1 = new JSONObject();
        task1.put("id",3L);
        task1.put("enqueueTime", currentTime - 100L);

        // "Old" Priority ID; RANK == 460
        JSONObject task2 = new JSONObject();
        task2.put("id",3L);
        task2.put("enqueueTime", currentTime - 200L);

        ResponseEntity<?> response = application.newTask(task1.toString());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        response = application.newTask(task2.toString());
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);

        response = application.poll();
        RankedTask responseTask = (RankedTask) response.getBody();
        assertEquals(3L, responseTask.getId());

        response = application.poll();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
    @Test
    public void testNegativeID() throws JSONException {
        long currentTime = Instant.now().getEpochSecond();

        // "New" Priority ID; RANK == 200
        JSONObject task = new JSONObject();
        task.put("id",-3L);
        task.put("enqueueTime", currentTime - 100L);

        ResponseEntity<?> response = application.newTask(task.toString());
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);

        response = application.poll();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testFutureEnqueueTime() throws JSONException {
        long currentTime = Instant.now().getEpochSecond();

        // "New" Priority ID; RANK == 200
        JSONObject task = new JSONObject();
        task.put("id",3L);
        task.put("enqueueTime", currentTime + 100L);

        ResponseEntity<?> response = application.newTask(task.toString());
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);

        response = application.poll();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
    @Test
    public void testMaximumCapacity() throws JSONException, InterruptedException {
        ResponseEntity<?> response;
        double prevRank = Double.MAX_VALUE;
        boolean isPreviousMgtOverride = false;
        long startTime = Instant.now().toEpochMilli();

        // Add maximum amount of tasks of random age with random IDs
        do {
            long id = (long)(Long.MAX_VALUE * Math.random());
            long currentTime = Instant.now().getEpochSecond();
            long age = (long)(3600 * Math.random()) + 1L; // Add 1 to make it always > 0

            JSONObject task = new JSONObject();
            task.put("id",id);
            task.put("enqueueTime", currentTime - age);

            response = application.newTask(task.toString());
        } while (response.getStatusCode() != HttpStatus.SERVICE_UNAVAILABLE);

        // Wait a while so the ranks change (the queue must be sorted accordingly though)
        Thread.sleep(5000);

        // Validate the listIds endpoint
        List<RankedTask> taskList = (List<RankedTask>)application.listIds().getBody();
        validateRankedList(taskList);

        // Poll tasks one by one and compare the rank with the previous one.
        // The rank may grow just once when we finish polling the Management Override
        // tasks (they must be at the top of the queue although their rank equals their age)
        do {
            response = application.poll();
            if (response.getStatusCode() == HttpStatus.OK) {
                RankedTask responseTask = (RankedTask) response.getBody();
                long currentTime = Instant.now().getEpochSecond();
                log.info("TEST: prevRank {} isPreviousMgtOverride {} taskId {} enqueueTime {} rank {} age {}",
                        prevRank, isPreviousMgtOverride, responseTask.getId(), responseTask.getEnqueueTime(),
                        responseTask.getCurrentRank(), currentTime - responseTask.getEnqueueTime());
                assertTrue(responseTask.getCurrentRank() <= prevRank || isPreviousMgtOverride);
                prevRank = responseTask.getCurrentRank();
                isPreviousMgtOverride = (responseTask.getId() % 15 == 0);
            }
        } while (response.getStatusCode() == HttpStatus.OK);

        long finishTime = Instant.now().toEpochMilli();
        log.info("testMaximumCapacity: The max capacity test took {} ms", finishTime - startTime);
    }

    private void insertTasksOfAllClasses() throws JSONException {
        long currentTime = Instant.now().getEpochSecond();

        // "New" Priority ID; RANK == 460
        JSONObject task1 = new JSONObject();
        task1.put("id",3L);
        task1.put("enqueueTime", currentTime - 100L);

        // "Old" Priority ID; RANK == 1059
        JSONObject task2 = new JSONObject();
        task2.put("id",9L);
        task2.put("enqueueTime", currentTime - 200L);

        // "New" Normal ID; RANK == 100
        JSONObject task3 = new JSONObject();
        task3.put("id",11L);
        task3.put("enqueueTime", currentTime - 100L);

        // "Old" Normal ID; RANK == 200
        JSONObject task4 = new JSONObject();
        task4.put("id",7L);
        task4.put("enqueueTime", currentTime - 200L);

        // "New" VIP ID; RANK == 921
        JSONObject task5 = new JSONObject();
        task5.put("id",25L);
        task5.put("enqueueTime", currentTime - 100L);

        // "Old" VIP ID; RANK == 2119
        JSONObject task6 = new JSONObject();
        task6.put("id",20L);
        task6.put("enqueueTime", currentTime - 200L);

        // "New" Management Override ID; Second highest
        JSONObject task7 = new JSONObject();
        task7.put("id",15L);
        task7.put("enqueueTime", currentTime - 100L);

        // "Old" Management Override ID; The highest
        JSONObject task8 = new JSONObject();
        task8.put("id",30L);
        task8.put("enqueueTime", currentTime - 200L);


        ResponseEntity<?> response = application.newTask(task1.toString());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        response = application.newTask(task2.toString());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        response = application.newTask(task3.toString());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        response = application.newTask(task4.toString());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        response = application.newTask(task5.toString());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        response = application.newTask(task6.toString());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        response = application.newTask(task7.toString());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        response = application.newTask(task8.toString());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    private void validateRankedList(List<RankedTask> list) {
        double prevRank = Double.MAX_VALUE;
        boolean isPreviousMgtOverride = false;
        while (!list.isEmpty()) {
            RankedTask task = list.get(0);
            assertTrue(task.getCurrentRank() <= prevRank || isPreviousMgtOverride);
            prevRank = task.getCurrentRank();
            if (task.getTaskClass() == RankedTask.TaskClass.MANAGEMENT_OVERRIDE) {
                isPreviousMgtOverride = true;
            }
            list.remove(task);
        }
    }
}
