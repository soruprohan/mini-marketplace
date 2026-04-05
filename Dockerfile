# ─────────────────────────────────────────────
# Stage 1 – build the JAR
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom first (layer-cache friendly)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies without building the app
RUN ./mvnw dependency:go-offline -B

# Copy source and build, skipping tests
COPY src/ src/
RUN ./mvnw package -DskipTests -B

# ─────────────────────────────────────────────
# Stage 2 – run the JAR (slim JRE image)
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runner

WORKDIR /app

# Copy only the fat JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Render sets PORT dynamically; default to prod profile
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]