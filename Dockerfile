FROM node:22-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-17 AS backend
WORKDIR /app/backend
COPY backend/pom.xml ./
RUN mvn -B dependency:go-offline
COPY backend/src/ ./src/
COPY --from=frontend /app/frontend/dist/ ./src/main/resources/static/
RUN mvn -B package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ENV TZ=Asia/Kolkata
RUN addgroup -S paisa && adduser -S paisa -G paisa
COPY --from=backend --chown=paisa:paisa /app/backend/target/paisa-1.0.0.jar /app/paisa.jar
USER paisa
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/paisa.jar"]
