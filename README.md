# CampusDesk — Mesa de ayuda de TechNova Solutions

Plataforma web para gestionar incidencias tecnológicas. Los usuarios reportan
problemas de Hardware, Software, Redes, Accesos u Otros y el equipo técnico los
atiende siguiendo un ciclo de vida controlado en el backend:

```
ABIERTA → ASIGNADA → EN_PROCESO → RESUELTA → CERRADA
```

Incluye comentarios, historial inmutable de cambios de estado, roles, autenticación
JWT y calificación del servicio una vez cerrado el ticket.

## Integrantes

- Julian Ovalle
- Camilo Vega
- Juan Bastos
- Crisian Rojas

## Tecnologías

| Capa | Tecnología |
|---|---|
| Frontend | HTML5 semántico, CSS3 modular y JavaScript Vanilla (`fetch()` + `async/await`). Sin frameworks ni librerías. |
| Backend | Java 17, Spring Boot 3.3, Spring Data JPA (Hibernate 6), Spring Security + JWT (jjwt 0.12), Bean Validation, springdoc-openapi (Swagger UI) |
| Base de datos | PostgreSQL 14+ |
| Pruebas | JUnit 5, Mockito, AssertJ |

## Arquitectura

```
campusdesk/
├── backend/                     API REST Spring Boot
│   └── src/main/java/com/technova/campusdesk/
│       ├── auth/                login, registro, /me
│       ├── ticket/              tickets: controller → service → repository → entity
│       ├── comment/             comentarios
│       ├── history/             historial de estados (solo inserción)
│       ├── feedback/            calificación de tickets cerrados
│       ├── report/              métricas del dashboard
│       ├── user/                usuarios y técnicos
│       ├── security/            filtro JWT, SecurityFilterChain, CORS, 401/403 JSON
│       ├── exception/           @RestControllerAdvice + excepciones de dominio
│       ├── config/              OpenAPI, inicialización de cuentas, validación de arranque
│       └── system/              diagnóstico del entorno
├── database/
│   ├── schema.sql               esquema completo (tablas, FK, índices, CHECK, triggers)
│   ├── seed.sql                 datos iniciales (admin, técnicos, usuarios, tickets)
│   └── migrations/              migraciones incrementales para bases existentes
└── frontend/
    ├── pages/                   login, registro, dashboard, tickets, crear, detalle, administración
    ├── css/                     tokens + estilos por módulo
    └── js/                      api.js (único punto con fetch), ui.js y un módulo por página
```

Cada módulo sigue la misma separación de responsabilidades:

- **Controller**: solo HTTP (rutas, `@Valid`, códigos de estado). Nunca devuelve entidades JPA.
- **Service**: reglas de negocio, permisos por rol y `@Transactional`.
- **Repository**: Spring Data JPA (`JpaRepository`, `Specification`, `@EntityGraph`, `@Query`).
- **Entity**: mapeo JPA. Los DTO son `record` con un método `from(entity)` que hace de mapper.

Todas las dependencias se inyectan por constructor.

## Requisitos previos

- JDK 17 o superior (probado con Java 21).
- PostgreSQL 14 o superior con `psql` en el `PATH`.
- Maven **no** hace falta: el proyecto incluye Maven Wrapper (`backend/mvnw`, `backend/mvnw.cmd`).
- Un servidor estático para el frontend en el puerto 5500 (Live Server de VS Code o `python -m http.server 5500`).

## 1. Base de datos

`database/schema.sql` es la única fuente de verdad del esquema. Hibernate solo lo
**valida** al arrancar (`spring.jpa.hibernate.ddl-auto=validate`) y nunca lo modifica.

```bash
# Crear usuario y base (una sola vez, como superusuario)
psql -U postgres -c "CREATE USER campusdesk_app WITH PASSWORD 'tu-clave';"
psql -U postgres -c "CREATE DATABASE technova_db OWNER campusdesk_app;"

# Esquema + datos iniciales
psql -U campusdesk_app -d technova_db -f database/schema.sql
psql -U campusdesk_app -d technova_db -f database/seed.sql
```

> **Aviso:** `schema.sql` empieza con `DROP TABLE ... CASCADE` para poder re-ejecutarse
> en desarrollo. Ejecútalo solo sobre una base nueva o de pruebas.
>
> Si tu base ya existía antes del módulo de calificaciones, no relances `schema.sql`:
> aplica la migración aditiva `database/migrations/20261023_add_ticket_feedback.sql`.

### Estructura de la base de datos

```
users 1 ──< tickets (requester_id)            ON DELETE RESTRICT
users 1 ──< tickets (assigned_technician_id)  ON DELETE RESTRICT, opcional
tickets 1 ──< comments        ON DELETE CASCADE      users 1 ──< comments (author_id)          RESTRICT
tickets 1 ──< ticket_history  ON DELETE CASCADE      users 1 ──< ticket_history (changed_by_id) RESTRICT
tickets 1 ──1 ticket_feedback ON DELETE CASCADE (clave primaria compartida)
```

| Tabla | Columnas principales | Restricciones e índices |
|---|---|---|
| `users` | `id`, `name`, `email`, `password` (hash BCrypt), `role`, `created_at` | `email` único (también en minúsculas), `CHECK role IN (ADMIN, TECHNICIAN, USER)`, índice por `role` |
| `tickets` | `id`, `title`, `description`, `category`, `priority`, `status`, `requester_id`, `assigned_technician_id`, `created_at`, `updated_at` | `CHECK` de enums y longitudes; `ABIERTA` ⇔ sin técnico; índices por solicitante, técnico, estado, prioridad, categoría y fecha |
| `comments` | `id`, `content`, `created_at`, `author_id`, `ticket_id` | índice `(ticket_id, created_at)` para listarlos en orden cronológico |
| `ticket_history` | `id`, `previous_status`, `new_status`, `changed_at`, `changed_by_id`, `ticket_id` | `previous_status` nulo solo en la creación; índice `(ticket_id, changed_at)` |
| `ticket_feedback` | `id` (= id del ticket), `puntuacion` 1–5, `comentario`, `fecha_creacion` | una calificación por ticket |

Triggers de integridad (defensa en profundidad, además del backend):

1. `updated_at` se actualiza solo.
2. Solo un usuario `TECHNICIAN` puede quedar asignado.
3. Un ticket `CERRADA` no puede modificarse.
4. No se aceptan comentarios en tickets `CERRADA`.
5. `ticket_history` es inmutable: no admite `UPDATE` ni `DELETE` manual.

## 2. Variables de entorno (backend)

| Variable | Obligatoria | Descripción |
|---|---|---|
| `DB_USER`, `DB_PASSWORD` | Sí | Credenciales de PostgreSQL |
| `JWT_SECRET` | Sí | Clave de firma JWT, mínimo 32 caracteres (sin valor por defecto: el arranque falla sin ella) |
| `DB_URL` | No | Por defecto `jdbc:postgresql://localhost:5432/technova_db` |
| `JWT_EXPIRATION_MINUTES` | No | Vida del token, por defecto 60 |
| `CORS_ALLOWED_ORIGINS` | No | Orígenes del frontend, por defecto `http://localhost:5500,http://127.0.0.1:5500` |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_NAME` | No | Crea la cuenta ADMIN al arrancar si no existe |
| `TECH_EMAIL`, `TECH_PASSWORD`, `TECH_NAME` | No | Crea una cuenta TECHNICIAN al arrancar si no existe |

Para desarrollo puedes copiar la plantilla, que git ignora, en lugar de exportar variables:

```bash
cp backend/src/main/resources/application-local.properties.example \
   backend/src/main/resources/application-local.properties
# reemplaza cada CAMBIAR_LOCALMENTE
```

`application.properties` la importa automáticamente si existe. Las variables de
entorno con el mismo nombre tienen prioridad. Nunca se suben contraseñas reales al repositorio.

## 3. Ejecución local

**Backend** (puerto 8080):

```bash
cd backend
./mvnw spring-boot:run        # Linux / macOS
mvnw.cmd spring-boot:run      # Windows
```

Desde IntelliJ IDEA: abre `backend/` como proyecto Maven y ejecuta
`com.technova.campusdesk.CampusDeskApplication`.

Al arrancar, `StartupValidator` comprueba el `JWT_SECRET`, la conexión a PostgreSQL y
que existan todas las tablas, y aborta con un mensaje claro si algo falta.
Diagnóstico sin iniciar sesión:

```bash
curl http://localhost:8080/api/system/status
```

**Frontend** (puerto 5500, el que permite CORS):

```bash
cd frontend
python -m http.server 5500
```

Abre `http://localhost:5500/pages/login.html`.

## 4. Cuentas de demostración

`database/seed.sql` crea estas cuentas, guardadas como hash BCrypt.
**Son solo para desarrollo y evaluación: cámbialas en cualquier otro entorno.**

| Rol | Correo | Contraseña |
|---|---|---|
| ADMIN | `admin@technova.com` | `Admin123*` |
| TECHNICIAN | `tecnico1@technova.com`, `tecnico2@technova.com` | `Tecnico123*` |
| USER | `usuario1@technova.com`, `usuario2@technova.com` | `Usuario123*` |

El registro público siempre crea cuentas `USER`. Los roles privilegiados solo
nacen de `seed.sql`, de las variables `ADMIN_*` / `TECH_*` (`DataInitializer`) o de
la promoción que hace un ADMIN desde el panel de administración.

## 5. Roles y reglas de negocio

| Rol | Puede |
|---|---|
| `USER` | Registrarse; crear tickets; ver solo los suyos; editarlos mientras estén `ABIERTA` y sin técnico; comentar; cerrar (`RESUELTA → CERRADA`); calificar un ticket cerrado |
| `TECHNICIAN` | Ver solo los tickets que tiene asignados; comentar; avanzar `ASIGNADA → EN_PROCESO → RESUELTA` |
| `ADMIN` | Ver todos los tickets, historiales y métricas globales; asignar y reasignar técnicos (`ABIERTA`/`ASIGNADA`); gestionar usuarios |

Todas las reglas se validan en la capa de servicio (`TicketService`), no en el frontend:

- Solo se avanza **un paso** del ciclo; saltar o retroceder estados responde `409`.
- `ASIGNADA` solo se alcanza con `PATCH /assign`; la reasignación queda en el historial.
- Cada cambio de estado guarda su fila en `ticket_history` dentro de la **misma transacción** (`@Transactional`): se guardan los dos o ninguno.
- `CERRADA` es definitivo y no admite comentarios.
- Aislamiento de datos: un USER que pide un ticket ajeno, o un TECHNICIAN que pide uno no asignado, recibe `403`.

## 6. API REST

Base: `http://localhost:8080/api`. Todas las rutas requieren `Authorization: Bearer <token>`
salvo las marcadas como públicas.

| Método | Ruta | Acceso | Respuesta |
|---|---|---|---|
| POST | `/auth/register` | Pública | 201 usuario creado (rol USER) |
| POST | `/auth/login` | Pública | 200 `{ token, tokenType, expiresInSeconds, user }` |
| GET | `/auth/me` | Autenticado | 200 usuario actual |
| GET | `/tickets?status=&priority=&category=` | Autenticado (filtrado por rol) | 200 lista |
| GET | `/tickets/{id}` | Con acceso al ticket | 200 |
| POST | `/tickets` | USER | 201 ticket en `ABIERTA` |
| PUT | `/tickets/{id}` | USER solicitante | 200 |
| PATCH | `/tickets/{id}/assign` | ADMIN | 200 ticket en `ASIGNADA` |
| PATCH | `/tickets/{id}/status` | Técnico asignado / solicitante | 200 |
| GET / POST | `/tickets/{id}/comments` | Con acceso al ticket | 200 / 201 |
| GET | `/tickets/{id}/history` | Con acceso al ticket | 200 (solo lectura) |
| POST | `/tickets/{id}/feedback` | USER solicitante, ticket `CERRADA` | 201 |
| GET | `/technicians/{id}/promedio-calificacion` | ADMIN | 200 |
| GET | `/reports/summary` | Autenticado (global para ADMIN) | 200 conteos por estado |
| GET | `/users`, `/users/technicians` | ADMIN | 200 |
| POST | `/users/{id}/technician` | ADMIN | 200 promueve USER a TECHNICIAN |
| GET | `/system/status` | Pública | 200 diagnóstico |

### Formato de errores

Todos los errores, incluidos 401 y 403 de Spring Security, devuelven el mismo JSON:

```json
{
  "timestamp": "2026-10-27T15:04:05Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Los datos enviados no son validos",
  "path": "/api/tickets",
  "details": ["title: El titulo debe tener entre 5 y 150 caracteres"]
}
```

| Código | Cuándo |
|---|---|
| 400 | Validación (`@Valid`), JSON mal formado, enum o id inválido |
| 401 | Sin token, token inválido o vencido, credenciales incorrectas |
| 403 | Rol sin permiso o ticket ajeno |
| 404 | Recurso o ruta inexistente |
| 409 | Regla de negocio (transición no permitida, correo duplicado, ticket ya calificado) |
| 500 | Error inesperado (sin trazas ni detalles internos) |

## 7. Swagger / OpenAPI

Con el backend en marcha: **http://localhost:8080/swagger-ui.html**
(especificación JSON en `http://localhost:8080/v3/api-docs`).

1. Ejecuta `POST /api/auth/login` con una cuenta de demostración.
2. Pulsa **Authorize** y pega el `token` (sin la palabra `Bearer`).

## 8. Seguridad

- Autenticación JWT sin estado (`SessionCreationPolicy.STATELESS`) con `JwtAuthenticationFilter` (`OncePerRequestFilter`).
- El rol se lee de la base de datos en cada petición, no del token: un cambio de rol aplica de inmediato.
- Contraseñas con `BCryptPasswordEncoder`; nunca se devuelven ni se registran en logs.
- `@PreAuthorize` en los endpoints por rol más validación de propiedad en los servicios.
- CORS explícito (`CorsConfigurationSource`) limitado a los orígenes de `CORS_ALLOWED_ORIGINS`.
- El frontend guarda el JWT en `localStorage`, lo envía en `Authorization: Bearer` y ante un 401 limpia la sesión y vuelve al login.

## 9. Pruebas

```bash
cd backend
./mvnw test          # mvnw.cmd test en Windows
```

Pruebas unitarias sin base de datos (Mockito):

- `TicketServiceTest`: ciclo de vida completo, pasos prohibidos, permisos por rol, aislamiento de datos e historial.
- `AuthServiceTest`: el registro siempre crea USER con hash BCrypt; el login no revela qué campo falló.
- `GlobalExceptionHandlerTest`: formato JSON de errores y códigos correctos (404/405 en lugar de 500).
