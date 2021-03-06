# Widgets

Widgets is an application that exposes a REST API to handle _Widgets_. A widget is any sort entity that can be represented in a 2-dimensional Euclidean Space. Additionally, widgets can be stacked on top of each other having their visibility determined by their z-index.

The functionality currently supported is limited to 2-d transformations of the widgets: coordinates manipulation, resizing and visibility. No UI is available at this time.

The application targets Java 8 and uses Spring Boot to implement the web layer. Maven manages the life-cycle of the application.

## Test

`./mvnw test` runs the unit tests  
`./mvnw integration-test` runs the unit+integration tests

## Run

`./mvnw spring-boot:run` will build _Widgets_ and bootstrap Tomcat bound to `localhost:8080`
