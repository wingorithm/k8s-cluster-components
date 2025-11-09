# ---- Stage 1: Build ----
FROM amazoncorretto:21-alpine-jdk AS builder

WORKDIR /app
RUN apk add --no-cache bash curl unzip wget

COPY . .
RUN ./mvnw clean package -DskipTests

# ---- Runtime ----
FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]