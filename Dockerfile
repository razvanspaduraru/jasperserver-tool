FROM eclipse-temurin:21.0.7_6-jdk as builder

WORKDIR /opt/app
COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN chmod 777 ./mvnw

COPY src ./src/
RUN ./mvnw clean install -f pom.xml

FROM eclipse-temurin:21.0.7_6-jdk
WORKDIR /opt/app
COPY --from=builder /opt/app/target/*.jar /opt/app/app.jar
ENTRYPOINT ["java", "-Xms4g", "-Xmx4g", "-jar", "/opt/app/app.jar"]
