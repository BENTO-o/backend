# Use the OpenJDK 17 slim base image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Install necessary dependencies (like findutils) and clean up to reduce image size
RUN apt-get update && apt-get install -y findutils \
    && rm -rf /var/lib/apt/lists/*

# Copy Gradle wrapper and settings first to leverage Docker cache
COPY gradlew /app/gradlew
COPY gradle /app/gradle
COPY build.gradle /app/
COPY settings.gradle /app/

# Make gradlew executable
RUN chmod +x ./gradlew

# Download dependencies without copying the entire project first (caching optimization)
RUN ./gradlew dependencies --no-daemon

# Copy the rest of the application
COPY . /app

# Build the application
RUN ./gradlew build --no-daemon

# Run the Spring Boot application
CMD ["java", "-jar", "build/libs/bento.jar"]
