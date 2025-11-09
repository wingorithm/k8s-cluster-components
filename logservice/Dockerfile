# ---- Stage 1: Build ----
FROM amazoncorretto:21-alpine-jdk as builder

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw

RUN ./mvnw dependency:go-offline

COPY src ./src
RUN ./mvnw package -DskipTests

# ---- Runtime ----
FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 9001

ENTRYPOINT ["java", "-jar", "app.jar"]