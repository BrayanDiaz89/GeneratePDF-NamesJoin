FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV PDF_OUTPUT_DIR=/app/output

RUN mkdir -p /app/output

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java","-jar","/app/app.jar"]


