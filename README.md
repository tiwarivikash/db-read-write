db-read-write
========

Building
--------

# Start Database
  ```
  cd database
  docker build -t db .
  docker run -p 5432:5432 -d db


# Start Application
  ```
  mvn spring-boot:run

# Post Request
  ```
  POST /employee/ HTTP/1.1
  Host: localhost:8080
  Accept: application/json
  Content-Type: application/json
  Cache-Control: no-cache

  {
  	"id":"1",
  	"name": "vikash",
  	"dept":"IT",
  	"salary": 2000
  }

# GET Request
  ```
  GET /employee/1 HTTP/1.1
  Host: localhost:8080
  Accept: application/json
  Cache-Control: no-cache
