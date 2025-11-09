# ---- Stage 1: Build ----
FROM amazoncorretto:21-alpine-jdk AS builder

WORKDIR /app

# Install dependencies for Maven Wrapper
RUN apk add --no-cache bash curl unzip wget

# Copy everything
COPY . .

# Fix permission AFTER copying
RUN chmod +x mvnw

# Build the app
RUN ./mvnw clean package -DskipTests

# ---- Runtime ----
FROM amazoncorretto:21-alpine-jdk

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
