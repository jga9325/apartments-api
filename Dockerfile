FROM amazoncorretto:17-alpine-jdk

ARG JAR=target/apartments-0.0.1-SNAPSHOT.jar

COPY $JAR app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]