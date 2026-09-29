# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

# ---- Runtime stage ----
FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN chown -R app:app /app
USER app

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]
