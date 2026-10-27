# Vivero Verde - Tienda de plantas (practica JPA)

Proyecto Spring Boot 3 + JPA + Lombok con arquitectura en capas.

## Tablas (5)

| Tabla             | Relaciones                                          |
|-------------------|-----------------------------------------------------|
| `categorias`      | 1 categoria -> N plantas                            |
| `plantas`         | N plantas -> 1 categoria                            |
| `clientes`        | 1 cliente -> N pedidos                              |
| `pedidos`         | N pedidos -> 1 cliente, 1 pedido -> N detalles      |
| `detalle_pedidos` | N detalles -> 1 pedido, N detalles -> 1 planta      |

## Capas

```
controller  -> recibe las peticiones HTTP
service     -> interfaces con la logica de negocio
service/impl-> implementaciones de los servicios
repository  -> acceso a datos (JpaRepository)
entity      -> clases @Entity (tablas)
dto         -> objetos que entran y salen de la API
mapper      -> convierte entity <-> dto
exception   -> errores propios y manejador global
```

## Ejecutar

```
mvn spring-boot:run
```

- API: http://localhost:8080/api
- Consola H2: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:viverodb`, usuario `sa`, sin clave)
- Los datos de ejemplo se cargan desde `src/main/resources/data.sql`.

## Endpoints

| Metodo | URL                                   | Descripcion                          |
|--------|---------------------------------------|--------------------------------------|
| GET/POST | `/api/categorias`                   | Listar / crear                       |
| GET/PUT/DELETE | `/api/categorias/{id}`        |                                      |
| GET/POST | `/api/plantas`                      | Listar / crear                       |
| GET/PUT/DELETE | `/api/plantas/{id}`           |                                      |
| GET    | `/api/plantas/categoria/{id}`         | Plantas de una categoria             |
| GET    | `/api/plantas/buscar?nombre=mon`      | Buscar por nombre                    |
| GET    | `/api/plantas/luz/{SOL\|MEDIA\|SOMBRA}` | Filtrar por luz                    |
| GET    | `/api/plantas/poco-stock?minimo=10`   | Consulta JPQL con @Query             |
| GET/POST | `/api/clientes`                     | Listar / crear                       |
| GET/PUT/DELETE | `/api/clientes/{id}`          |                                      |
| GET    | `/api/clientes/{id}/pedidos`          | Historial de un cliente              |
| GET/POST | `/api/pedidos`                      | Listar / crear (descuenta stock)     |
| GET/DELETE | `/api/pedidos/{id}`               |                                      |
| GET    | `/api/pedidos/estado/{estado}`        | Filtrar por estado                   |
| PATCH  | `/api/pedidos/{id}/estado?valor=ENVIADO` | Cambiar estado (CANCELADO devuelve stock) |
| GET    | `/api/detalles/{id}`                  | Ver un detalle                       |
| GET    | `/api/detalles/pedido/{pedidoId}`     | Detalles de un pedido                |

## Reglas de negocio

- Ciclo de un pedido: `PENDIENTE -> ENVIADO -> ENTREGADO`, y `PENDIENTE/ENVIADO -> CANCELADO`.
  `ENTREGADO` y `CANCELADO` son estados finales.
- Crear un pedido descuenta stock; cancelarlo lo devuelve. Si una linea falla, no se guarda nada (rollback).
- Solo se pueden eliminar pedidos `CANCELADO`.
- No se puede borrar una categoria con plantas, una planta que aparece en pedidos ni un cliente con pedidos.
- `Planta` usa `@Version` (bloqueo optimista) para evitar vender mas stock del que hay con compras simultaneas.

## Tests

```
mvn test
```

- `PedidoServiceIntegridadTest`: stock, rollback, ciclo de estados.
- `ConsultasSinN1Test`: comprueba que cada listado se resuelve con 1 sola consulta SQL.

### Ejemplo: crear un pedido

```json
POST /api/pedidos
{
  "clienteId": 2,
  "detalles": [
    { "plantaId": 3, "cantidad": 2 },
    { "plantaId": 5, "cantidad": 4 }
  ]
}
```
