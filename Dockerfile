# initialize build and set base image for first stage
FROM maven:3-eclipse-temurin-17-alpine as build
# speed up Maven JVM a bit
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
# set working directory
WORKDIR /home/app
# copy just pom.xml
COPY pom.xml .
# go-offline using the pom.xml
RUN mvn dependency:go-offline
# copy your other files
COPY ./src ./src
# compile the source code and package it in a jar file
RUN mvn clean install -Dmaven.test.skip=true
#Stage 2
# set base image for second stage
FROM amazoncorretto:17-alpine-jdk
# set deployment directory
WORKDIR /home/app
# copy over the built artifact from the maven image
COPY --from=build /home/app/target/spring-boot-app*.jar /home/app/spring-boot-app.jar

CMD ["java", "-jar", "/home/app/spring-boot-app.jar"]



