# Librarium Marginalia

Librarium Marginalia es una plataforma colaborativa para reseñas, análisis literario y debates sobre libros. El backend expone una API REST con Spring Boot y persiste la información en MySQL.

## Estado técnico actual

- **Java:** 25 (`pom.xml`)
- **Spring Boot:** 4.1.2
- **Build:** Maven (`./mvnw`)
- **Web:** Spring MVC
- **Persistencia:** Spring Data JPA/Hibernate
- **Base de datos:** MySQL/MariaDB, configurada en `src/main/resources/application.properties`
- **Validación:** Jakarta Validation
- **JSON:** Jackson mediante Spring MVC
- **Documentación API:** Springdoc OpenAPI/Swagger UI
- **Código backend:** `src/main/java/ovh/emserv/librarium/`
- **Aplicación existente:** `ovh.emserv.librarium.LibrariumApplication`

La compilación requiere un JDK compatible con Java 25. El proyecto no debe documentarse como Java 17/Spring Boot 3 mientras `pom.xml` mantenga esta configuración.

## Estructura del backend

```text
src/main/java/ovh/emserv/librarium/
├── LibrariumApplication.java
├── config/
│   └── CorsConfig.java
├── entity/
├── repository/
├── service/
├── controller/
└── dto/
```

La arquitectura prevista es:

`Controller -> Service -> Repository`

Las entidades usan JPA y Lombok. Las fechas se inicializan con `@PrePersist`. Los controladores no acceden directamente a repositorios.

## Funcionalidad disponible

- `GET /api/libros`
- `GET /api/libros/{id}`
- `GET /api/libros/buscar?q=...`
- `GET /api/libros/buscar-externo?q=...`
- `POST /api/libros/importar`
- `POST /api/resenas`
- `GET /api/resenas?libroId=...&usuarioId=...`
- `PUT /api/resenas/{id}`
- `DELETE /api/resenas/{id}`
- `GET /api/anotaciones?libroId=...`
- `POST /api/anotaciones`
- `PUT /api/anotaciones/{id}`
- `DELETE /api/anotaciones/{id}`
- `GET /api/foros?libroId=...`
- `POST /api/foros`
- `PUT /api/foros/{id}`
- `DELETE /api/foros/{id}`
- `GET /api/foros/{hiloId}/respuestas?page=0&size=20`
- `POST /api/foros/{hiloId}/respuestas`
- `PUT /api/foros/respuestas/{id}`
- `DELETE /api/foros/respuestas/{id}`

La integración con Open Library se realiza actualmente mediante `java.net.http.HttpClient`. La búsqueda externa no persiste datos; la importación recibe el resultado seleccionado y lo guarda junto con sus autores. La autenticación JWT, BCrypt, autorización por roles y la clave persistente mediante `LIBRARIUM_JWT_SECRET` están operativas.

## Modelo de datos

El esquema fuente es `database/scriptLibrarium.sql` y contiene ocho tablas:

- `usuarios`: `id`, `username`, `email`, `password_hash`, `rol`, `fecha_registro`.
- `libros`: `id`, `open_library_key`, `isbn`, `titulo`, `sinopsis`, `ano_publicacion`, `portada_url`, `fecha_registro`.
- `autores`: autores reutilizables identificados por nombre.
- `libros_autores`: tabla puente para la relación de varios autores por libro.
- `resenas`: relación con `libros` y `usuarios`, puntuación, comentario y `fecha_registro`.
- `anotaciones_guias`: relación con `libros` y `usuarios`, capítulo, página, tipo, contenido y `fecha_creacion`.
- `foro_hilos`: relación con `libros` y `usuarios`, título, contenido inicial y `fecha_creacion`.
- `foro_respuestas`: relación con `foro_hilos` y `usuarios`, contenido y `fecha_creacion`.

Las claves primarias del script SQL usan `BIGINT AUTO_INCREMENT`, compatible con `Long` en las entidades Java. `open_library_key` tiene una restricción `UNIQUE` para evitar duplicados.

## Documentación interactiva

Con la aplicación iniciada:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Documento OpenAPI: `http://localhost:8080/v3/api-docs`

## Configuración y ejecución

1. Crear la base de datos ejecutando `database/scriptLibrarium.sql`.
2. Definir `LIBRARIUM_JWT_SECRET` con una clave Base64 de al menos 32 bytes. Para desarrollo local, copia `.env.example` como `.env` y sustituye el valor; `.env` está ignorado por Git.
3. Revisar las credenciales de `src/main/resources/application.properties`.
4. Iniciar el contenedor MySQL en `localhost:3308`.
5. Ejecutar `./mvnw spring-boot:run`.
6. Usar el puerto `8080`, salvo que se modifique `server.port`.

No se deben versionar credenciales reales. La configuración local debe migrarse a variables de entorno o perfiles de Spring antes de publicar el proyecto.

## Próximos bloques técnicos

1. Añadir más pruebas de integración MockMvc y repositorios.
2. Versionar las migraciones de base de datos.
3. Completar ejemplos de permisos y errores en OpenAPI.

La guía interna de ejecución está en [`PROCESOS.md`](PROCESOS.md). El seguimiento público de cada bloque se realiza mediante GitHub Issues.
