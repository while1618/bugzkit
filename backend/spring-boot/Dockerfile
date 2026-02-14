FROM maven:sapmachine AS build
RUN apt-get update; apt-get install -y curl \
    && curl -sL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && curl -L https://www.npmjs.com/install.sh | sh
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre AS dev
WORKDIR /app
COPY --from=build /app/target/*.jar bugzkit-api.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "bugzkit-api.jar"]

FROM eclipse-temurin:21-jre AS prod
WORKDIR /app
COPY --from=build /app/target/*.jar bugzkit-api.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "bugzkit-api.jar"]
