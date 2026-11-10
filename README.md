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
- `GET /api/anotaciones?libroId=...`
- `POST /api/anotaciones`
- `GET /api/foros?libroId=...`
- `POST /api/foros`
- `GET /api/foros/{hiloId}/respuestas`

La integración con Open Library se realiza actualmente mediante `java.net.http.HttpClient`. La búsqueda externa no persiste datos; la importación recibe el resultado seleccionado y lo guarda junto con sus autores. La autenticación JWT, BCrypt y la autorización por roles están operativas; la clave JWT todavía debe externalizarse para conservar los tokens entre reinicios.

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
2. Revisar las credenciales de `src/main/resources/application.properties`.
3. Ejecutar `./mvnw spring-boot:run`.
4. Usar el puerto `8080`, salvo que se modifique `server.port`.

No se deben versionar credenciales reales. La configuración local debe migrarse a variables de entorno o perfiles de Spring antes de publicar el proyecto.

## Próximos bloques técnicos

1. Externalizar la clave JWT y no regenerarla al reiniciar.
2. Crear DTOs de respuesta para no exponer entidades ni `passwordHash`.
3. Añadir consulta de reseñas y creación de respuestas de foro.
4. Completar autorización de propietario y administrador.
5. Añadir pruebas unitarias y de integración.

La guía interna de ejecución está en [`PROCESOS.md`](PROCESOS.md). El seguimiento público de cada bloque se realiza mediante GitHub Issues.
