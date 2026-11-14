# ----------------------------------------------------
# Stage 1: Build & Package with Maven and Java 21 LTS
# ----------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build executable jar
COPY src ./src
RUN ./mvnw clean package -DskipTests=false -B

# ----------------------------------------------------
# Stage 2: Lightweight Production Runtime Container
# ----------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Set non-root user for security
RUN addgroup -S dama && adduser -S dama -G dama
USER dama

# Copy compiled JAR from builder stage
COPY --from=builder /app/target/*.jar /app/app.jar

# Expose default matchmaking port
EXPOSE 8989

# Environment variable defaults
ENV PORT=8989

# Launch headless server entry point
ENTRYPOINT ["java", "-cp", "/app/app.jar", "com.dama.ethiopiancheckersgame.network.ServerMain"]
