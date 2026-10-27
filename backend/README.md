# CampusDesk · Backend (Java + Spring Boot)

API REST de la mesa de ayuda. El frontend (`../frontend`) la consume con `fetch()`.

La guía completa (base de datos, variables de entorno, cuentas demo, endpoints,
formato de errores y seguridad) está en el [README principal](../README.md).

## Arranque rápido

```bash
# 1. Esquema y datos (una sola vez, sobre una base vacía)
psql -U campusdesk_app -d technova_db -f ../database/schema.sql
psql -U campusdesk_app -d technova_db -f ../database/seed.sql

# 2. Variables obligatorias: DB_USER, DB_PASSWORD y JWT_SECRET (32+ caracteres),
#    exportadas o en src/main/resources/application-local.properties

# 3. Ejecutar
./mvnw spring-boot:run      # mvnw.cmd en Windows
```

- API: `http://localhost:8080/api`
- Swagger: `http://localhost:8080/swagger-ui.html`
- Diagnóstico público: `GET http://localhost:8080/api/system/status`

## Pruebas

```bash
./mvnw test
```
