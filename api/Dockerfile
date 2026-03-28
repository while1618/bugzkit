FROM eclipse-temurin:25-jdk AS build
RUN apt-get update && apt-get install -y curl \
    && curl -sL https://deb.nodesource.com/setup_22.x | bash - \
    && apt-get install -y nodejs
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY src ./src
RUN ./mvnw clean install -DskipTests

FROM eclipse-temurin:25-jre AS dev
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/*.jar bugzkit-api.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "bugzkit-api.jar"]

FROM eclipse-temurin:25-jre AS prod
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/*.jar bugzkit-api.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "bugzkit-api.jar"]
