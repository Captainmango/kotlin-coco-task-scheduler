POST http://localhost:8080/api/v1/tasks
{
  "cron": "* * * * *",
  "cmd": "echo 'hello world'"
}

GET http://localhost:8080/api/v1/tasks/999b71e6-a6ff-4d9d-afe0-a320cd15366d