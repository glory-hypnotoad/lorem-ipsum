
# Priority Queue Coding Assignment

## Description of Service

The **Lorem Ipsum** REST service implements a Priority Queue for Task objects:

 - Each Task consists of ID (positive Long) and enqueueTime (positive Long **which represents the UTC time in Unix epoch format**)
 - Tasks class is determined based on the Task ID: if ID is evenly divisible by 3 AND 5 the Task is called **Management Override**; if ID is evenly divisible by 5 only the Task is called **VIP**; if ID is evenly divisible by 3 only the Task is called **Priority**; all other Tasks are called **Normal**
 - The Tasks in the queue are sorted based on their Rank which, in turn, depends on the Task class
 - Management Override tasks are always at the top of the queue; among themselves they are sorted based on their age (number of seconds sitting in the queue)
 - VIP tasks rank is calculated as ***max(4; 2n ln n)***
 - Priority tasks rank is calculated as ***max(3; n ln n)***
 - Normal tasks rank, again, equals their age in queue in seconds

The Lorem Ipsum REST service provides the following endpoints:

### **POST "/newtask"**

 The endpoint accepts POST requests with JSON body of the following format:

```
    {
        "id":11,
        "enqueueTime":1665657000
    }
```

where id is the new task ID (positive Long) and enqueueTime is the time when the task is considered being enqueued. Returns:
 - "200 OK" if the task added successfully
 - "503 Service Unavailable" if the queue is full **(maximum supported number is 1000 tasks)**
 - "400 Bad Request" if failed to add the task. In this case the response body contains JSON object of the following format:
 ```
{"status":"E_ID_ALREADY_EXISTS"}
```
where possible statuses are:
```
E_NEGATIVE_ID,  
E_INVALID_ENQUEUE_TIME,  
E_ID_ALREADY_EXISTS,  
E_RANKED_TASK_ALREADY_EXISTS,  
E_QUEUE_FULL
```

### GET "/poll"
This endpoint returns:
 - "200 OK" with JSON body representing the highest ranked task in the following format:
```
{
    "id": 30,
    "enqueueTime": 1665657000,
    "currentRank": 284803.0
}
```
 - "404 Not Found" if the queue is empty

### GET "/listIds"
This endpoint returns:

 - "200 OK" with JSON body consisting of the full list of tasks in the queue in the following format:
```
[
    {
        "id": 30,
        "enqueueTime": 1665657000,
        "currentRank": 284767.0
    },
    {
        "id": 20,
        "enqueueTime": 1665657000,
        "currentRank": 7153020.458394589
    }
]
```
 - "404 Not Found" if the queue is empty

### GET "/position/{id}"
This endpoint returns:

 - "200 OK" with JSON body containing only "position" field if the task was found:
```
{"position":0}
```
 - "404 Not Found" otherwise

### DELETE "/task/{id}"
This endpoint deletes the task with the specified ID (if the task is enqueued). The endpoint returns:

 - "200 OK" if the task was successfully deleted
 - "404 Not Found" if the task with the specified ID is not enqueued. The JSON body contains the *status == E_TASK_NOT_FOUND*

### GET "/ewt"
This endpoint returns "200 OK" with JSON body containing only one field:
```
{"EWT":286683}
```
The returned value is an average (mean) number of seconds that each ID has been waiting in the queue. If the queue is empty then EWT = 0.

## Prerequisites
To build and run the service locally the following is required:
 - Java 19
 - Maven

## Build
To build the project locally run the following command from the root lorem-ipsum directory:
```
mvnw package
```
The war file will be created in the *target* directory

## Local Running
To run the project locally execute the command from the root project directory:
```
mvnw spring-boot:run
```
The service will run on localhost:8080

## Internal Implementation
The service is built on [Red-Black trees](https://en.wikipedia.org/wiki/Red%E2%80%93black_tree) which provide logarithmic complexity for such operations as "Insert" and "Poll". This allows to achieve much higher performance in comparison with more simple implementations like linear queue.

The Red-Black tree logic is implemented in the *RedBlackTree* generic class that incorporates all basic tree operations (such as Insert, Poll, Delete, Find Maximum/Minimum). This class maintains all Red-Black tree properties after each operation.

The Queue logic is implemented in the *TaskPriorityQueue* class. It has 5 (five) Red-Black trees containing the following:

 - ID tree consists of all IDs (and only IDs) that are presently enqueued
 - Four Ranked Task trees - one for each Task class.

Four separate Ranked Task trees are required because in case of a single one the ranking breaks after a period of time: e.g. a Priority task may be ranked lower than a Normal one upon inserting into the queue but the Priority becomes higher than the other after a period of time. Therefore, we have four separate trees - one for each Task class - and once Poll occurs we get the highest ranked Task from every tree and return the highest ranked among them.

In the above architecture getting a Task's position or building the whole list of enqueued Tasks is a more complex operation from the algorithmic complexity point of view: the four trees need to be re-organized into lists (which implies, at minimum, linear complexity) and the merged into a single list (linear again).

Getting the Expected Wait Time (EWT) is implemented in a more simple way: we store a sum of all enqueueTime values and at any given time the average EWT may be calculated by the following formula:
```
ewt = currentTime - (sumEnqueueTime / n);
```
Finally, the endpoints mapping is implemented in the main *LoremIpsumApplication* class which does not incorporate any business logic but provides just the REST interface to the service.