FROM openjdk:17-jdk-slim

# Step 2: Set the working directory inside the container
WORKDIR /app

# Step 3: Copy the jar file from the target directory into the container
# This assumes you have already built your Spring Boot app and generated a JAR file
COPY target/project71-0.0.1-SNAPSHOT.war app.war

# Step 4: Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.war"]

# Step 5: Expose port 8080 to make the app accessible
EXPOSE 8080