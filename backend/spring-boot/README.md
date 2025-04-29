## Running the Backend

### Running Locally

For the API to start properly, you'll need Postgres and Redis running. The easies way to start them is via Docker. Just run `docker-compose-db.dev.yml`:

```bash
docker-compose -f docker-compose-db.dev.yml up -d
```

After the services are up and running, start the application:

```bash
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Alternatively, use IntelliJ IDEA. A pre-configured run configuration is available - just click and run!

### Running via Docker

For running the API via Docker, execute:

```bash
docker-compose -f docker-compose-api.dev.yml up --build -d
```

**Note**: Email functionalities require SMTP configuration. Refer to the Environment Variables section for setup.
