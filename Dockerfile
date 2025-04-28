# ---- Build Stage ----
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copie o wrapper e o pom
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
# Copie o restante do código
COPY src src

# Build do projeto (gera o JAR no diretório target)
RUN ./mvnw -B clean package -DskipTests

# ---- Run Stage ----
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copie o JAR construído
COPY --from=build /app/target/*.jar app.jar

# Exponha a porta padrão do Spring Boot
EXPOSE 8080

# Variável de ambiente para profile (pode ser sobreposta)
ENV SPRING_PROFILES_ACTIVE=prod

# Comando de inicialização
ENTRYPOINT ["java","-jar","app.jar"]
