# ===================================================================
# DOCKERFILE PARA BACKEND (SPRING BOOT 3 + JAVA 17)
# ===================================================================

# Etapa 1: Compilación y empaquetado con Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Copiar descriptor de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final optimizada y ligera de ejecución con JRE
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Crear usuario sin privilegios por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copiar el archivo JAR generado desde la etapa de compilación
COPY --from=builder /app/target/*.jar app.jar

# Exponer el puerto del servidor HTTP / WebSocket
EXPOSE 8080

# Comando de arranque de la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
