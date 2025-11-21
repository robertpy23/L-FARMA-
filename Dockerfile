
FROM openjdk:17-jdk-slim
ARG JAR_FILE=target/Lfarma-0.0.1-SNAPSHOT.jar

# Directorio dentro del contenedor
WORKDIR /app

# Copiar el archivo JAR generado
COPY target/Lfarma-0.0.1-SNAPSHOT.jar Lfarma.jar

# Exponer el puerto de la app
EXPOSE 8091

# Comando para ejecutar tu aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]