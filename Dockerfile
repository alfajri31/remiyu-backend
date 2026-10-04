FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system remiyu && useradd --system --gid remiyu remiyu
COPY --from=build /app/target/remiyu-backend-0.1.0-SNAPSHOT.jar /app/app.jar
USER remiyu
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]

