FROM maven:3.9-eclipse-temurin-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-jammy
# Copy the built jar, using wildcard to avoid version dependency
COPY --from=build /target/*.jar app.jar
# The application will use the PORT environment variable if set (Koyeb sets this), otherwise 9090
EXPOSE 9090
ENTRYPOINT ["java","-jar","/app.jar"]
