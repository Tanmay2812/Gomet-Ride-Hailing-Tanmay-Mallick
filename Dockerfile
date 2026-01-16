# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Add New Relic agent (optional)
# Download from: https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip
# COPY newrelic/newrelic.jar /app/newrelic.jar
# COPY newrelic/newrelic.yml /app/newrelic.yml

COPY --from=build /app/target/ride-hailing-1.0.0.jar app.jar

EXPOSE 8080

# Without New Relic
ENTRYPOINT ["java", "-jar", "app.jar"]

# With New Relic (uncomment to enable)
# ENTRYPOINT ["java", "-javaagent:/app/newrelic.jar", "-jar", "app.jar"]
