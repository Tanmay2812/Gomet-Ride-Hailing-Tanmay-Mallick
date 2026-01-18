# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY backend ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Add New Relic agent
COPY newrelic/newrelic/newrelic.jar /app/newrelic.jar
COPY newrelic/newrelic/newrelic.yml /app/newrelic.yml

COPY --from=build /app/target/ride-hailing-1.0.0.jar app.jar

EXPOSE 8080

# With New Relic agent
ENTRYPOINT ["java", "-javaagent:/app/newrelic.jar", "-jar", "app.jar"]
