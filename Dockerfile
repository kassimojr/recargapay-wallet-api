# ---- Build Stage ----
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copy the wrapper and pom
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
# Copy the rest of the code
COPY src src

# Build the project (generates the JAR in the target directory)
RUN ./mvnw -B clean package -DskipTests

# ---- Run Stage ----
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR
COPY --from=build /app/target/*.jar app.jar

# Expose default Spring Boot port
EXPOSE 8080

# Environment variable for profile (can be overridden)
ENV SPRING_PROFILES_ACTIVE=prod

# Startup command
ENTRYPOINT ["java","-jar","app.jar"]
