# challenge

To make this app deliverable some improvements would be:

+ Changing from ConcurrentHashMap to a relational database, but keeping in mind that changes would have to be done regarding concurrency since the current implementation is using ConcurrentHashMap which is thread-safe and to keep it simple as possible the solution to solve the challenge to avoid deadlocks was to only make changes one account at a time since ConcurrentHashMap locks the key that is being updated.
+ Another change regarding the data acess layer would be the usage of Spring Data JPA since it offers features like locking which would help solve concurrency problems, and the usage of Liquidbase to track changes made to the database. 
+ To have a consistent documentation so the users of the API would know what each endpoint does by using a tool like Swagger.
+ Adding a ControllerAdvice so all the exception handling at controllerlevel would be done at one place instead of having to use try-catch.
+ Containerize the applications into docker images for ease of deployment using plugins like jib since these images dont depend on the enviroment.
+ And by developing a CI/CD workflow to help automate tasks like testing and deployment.

