application.properties example


```
spring.application.name=ellipse

spring.datasource.url=jdbc:postgresql://localhost:5432/ellipse_db
spring.datasource.username=db_user_example
spring.datasource.password=db_password_example
spring.datasource.driver-class-name=org.postgresql.Driver
spring.mvc.log-request-details=true

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

logging.level.eu.ellipse=DEBUG
logging.level.org.springframework=DEBUG

jwt.secret=random-secret-here
```


TODO:

1. create unified file in `exception` to store all error codenames
2. implement token refresh