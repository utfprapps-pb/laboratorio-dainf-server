FROM openjdk:11-jre
ADD target/server-0.1.jar server.jar
ENTRYPOINT ["java", "-jar", "server.jar"]