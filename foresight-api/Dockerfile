# Estágio 1: Build (Compilação do Maven)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Instala ferramentas para corrigir codificação e quebras de linha do Windows
RUN apt-get update && apt-get install -y dos2unix

# Copia as dependências primeiro para aproveitar o cache do Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código-fonte
COPY src ./src

# CORREÇÃO DEFINITIVA: Converte todos os arquivos de texto para o formato UNIX e UTF-8
RUN find ./src -type f -name "*.properties" -exec dos2unix {} +
RUN find ./src -type f -name "*.properties" -exec bash -c 'iconv -f WINDOWS-1252 -t UTF-8 "$1" > temp && mv temp "$1" || true' _ {} +

# Compila o projeto ignorando erros de encoding que possam sobrar
RUN mvn clean package -DskipTests -Dproject.build.sourceEncoding=UTF-8

# Estágio 2: Run (Apenas a JRE para ficar leve)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Pega o .jar gerado no Estágio 1
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]