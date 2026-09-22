FROM eclipse-temurin:24-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw

RUN ./mvnw -B -DskipTests clean package

EXPOSE 8080

CMD ["sh", "-c", "java -jar target/*.jar"]