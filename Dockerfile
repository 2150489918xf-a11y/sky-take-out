FROM openjdk:8-jdk-alpine

WORKDIR /app

COPY sky-server/target/sky-server-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
