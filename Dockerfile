# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Run stagedffd
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the jar file. ensuring we look in the correct directory /app/target
COPY --from=build /app/target/Smartparking-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
