git# Use a suitable base image with JDK
FROM openjdk:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the project files into the container
COPY . /app

# Install Maven
RUN apk add --no-cache maven

# Build the application using Maven
RUN mvn package

# Expose the application port (assuming 8080)
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/webscraper-0.0.1-SNAPSHOT.jar"]
