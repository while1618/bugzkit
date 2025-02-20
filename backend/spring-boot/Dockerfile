FROM maven:sapmachine AS build
RUN apt-get update; apt-get install -y curl \
    && curl -sL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && curl -L https://www.npmjs.com/install.sh | sh
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean install -DskipTests

FROM openjdk:21-jdk AS dev
WORKDIR /app
COPY --from=build /app/target/*.jar bugzkit-api.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "bugzkit-api.jar"]

FROM openjdk:21-jdk AS prod
WORKDIR /app
COPY --from=build /app/target/*.jar bugzkit-api.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "bugzkit-api.jar"]
