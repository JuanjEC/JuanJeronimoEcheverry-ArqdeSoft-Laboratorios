FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY target/banco.jar banco.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "banco.jar"]
