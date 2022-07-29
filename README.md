# challenge

To make this app deliverable some improvements would be:

+ Changing from ConcurrentHashMap to a relational database and use Spring Data JPA to help implement the data acess layer and use Liquidbase to track changes.
+ Add Swagger to document all the endpoints.
+ Add ControllerAdvice so all the exception handling at controller level would be done at one place.
+ Containerize the applications into docker images for ease of deployment using plugins like jib.
+ Develop a CI/CD workflow to help automate tasks like testing and deployment.

