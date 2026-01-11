FROM eclipse-temurin:21-jre-alpine-3.23

WORKDIR /app
COPY build/libs/ums-1.3.jar app.jar

EXPOSE 9001
ENTRYPOINT ["java", "-jar", "app.jar"]