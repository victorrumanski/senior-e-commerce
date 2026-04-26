## catalog-service

Starter Spring Boot microservice for the e-commerce fleet.

### Local

Start infra:

```bash
cd ../../infra
docker compose up -d
```

Build/run via Docker (no local JDK 25 required):

```bash
docker build -t catalog-service:local .
docker run --rm -p 8083:8080 \
  -e DB_SCHEMA=catalog \
  -e DB_HOST=host.docker.internal -e DB_NAME=ecomm -e DB_USER=ecomm -e DB_PASSWORD=ecomm \
  -e KAFKA_HOST=host.docker.internal \
  catalog-service:local
```
