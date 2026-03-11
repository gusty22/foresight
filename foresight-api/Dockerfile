FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
RUN apt-get update && apt-get install -y dos2unix
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN find ./src -type f -name "*.properties" -exec dos2unix {} +
RUN find ./src -type f -name "*.properties" -exec bash -c 'iconv -f WINDOWS-1252 -t UTF-8 "$1" > temp && mv temp "$1" || true' _ {} +
RUN mvn clean package -DskipTests -Dproject.build.sourceEncoding=UTF-8
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]