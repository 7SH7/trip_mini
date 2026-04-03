FROM eclipse-temurin:21-jre-alpine AS base
RUN apk add --no-cache ffmpeg
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
