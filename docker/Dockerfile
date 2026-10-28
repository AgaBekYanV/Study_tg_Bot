FROM eclipse-temurin:21-jre-alpine

ARG JAR_FILE=*.jar

COPY ${JAR_FILE} application.jar

ENTRYPOINT ["java", "-jar", "application.jar"]

EXPOSE 8080