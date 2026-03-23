# Use OpenJDK 11
FROM openjdk:11-jre-slim

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY target/smartcity-dechets-1.0-SNAPSHOT.jar app.jar

# Expose port if needed (for future web version)
# EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]