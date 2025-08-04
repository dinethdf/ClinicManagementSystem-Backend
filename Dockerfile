# ---- Build Stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Copy project files
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build the JAR
RUN mvn clean package -DskipTests

# ---- Run Stage ----
FROM eclipse-temurin:17-jre-alpine

# Change this if your JAR name is different
ARG JAR_FILE=target/*.jar

# Copy the jar from the build stage
COPY --from=build /app/${JAR_FILE} app.jar

# Expose port (Render automatically listens to PORT env, but this is fine)
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]
