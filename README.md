
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
 - "503 Service Unavailable" if the queue is full (maximum supported number is 1000 tasks)
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
E_TASK_NOT_FOUND,  
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
