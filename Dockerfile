# BUILD
FROM eclipse-temurin:21-jdk-alpine as build
WORKDIR /workspace/labs

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
COPY report report

# clean up the file
RUN sed -i 's/\r$//' mvnw
# create package
RUN /bin/sh mvnw package -DskipTests

# DELIVERY
FROM openjdk:21-ea-jdk
COPY --from=build /workspace/labs/target/server-0.1.jar server.jar
ENTRYPOINT ["java", "-jar", "server.jar"]