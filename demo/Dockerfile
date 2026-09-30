# Fase 1: Build dell'applicazione
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# Entra nella cartella demo prima di compilare
WORKDIR /app/demo
RUN mvn clean package -DskipTests

# Fase 2: Esecuzione
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Prende il jar compilato dentro la cartella demo/target
COPY --from=build /app/demo/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]