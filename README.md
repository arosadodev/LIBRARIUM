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
- **Código backend:** `src/main/java/com/librarium/`
- **Aplicación existente:** `ovh.emserv.librarium.LibrariumApplication`
- **API adicional:** `com.librarium.ApiApplication`

La compilación requiere un JDK compatible con Java 25. El proyecto no debe documentarse como Java 17/Spring Boot 3 mientras `pom.xml` mantenga esta configuración.

## Estructura del backend

```text
src/main/java/com/librarium/
├── ApiApplication.java
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
- `GET /api/resenas?libroId=...`
- `POST /api/resenas`
- `GET /api/anotaciones?libroId=...`
- `POST /api/anotaciones`
- `GET /api/foros?libroId=...`
- `POST /api/foros`
- `GET /api/foros/{hiloId}/respuestas`

La integración con Open Library se realiza actualmente mediante `java.net.http.HttpClient`. La autenticación JWT y la autorización por roles están preparadas como objetivo técnico, pero todavía deben terminar de implementarse en la API.

## Modelo de datos

El esquema fuente es `database/scriptLibrarium.sql` y contiene seis tablas:

- `usuarios`: `id`, `username`, `email`, `password_hash`, `rol`, `fecha_registro`.
- `libros`: `id`, `open_library_key`, `isbn`, `titulo`, `autor`, `sinopsis`, `ano_publicacion`, `portada_url`, `fecha_registro`.
- `resenas`: relación con `libros` y `usuarios`, puntuación, comentario y `fecha_registro`.
- `anotaciones_guias`: relación con `libros` y `usuarios`, capítulo, página, tipo, contenido y `fecha_creacion`.
- `foro_hilos`: relación con `libros` y `usuarios`, título, contenido inicial y `fecha_creacion`.
- `foro_respuestas`: relación con `foro_hilos` y `usuarios`, contenido y `fecha_creacion`.

Las claves primarias del script SQL son `INT AUTO_INCREMENT`; las entidades Java usan `Long`, por lo que este punto debe normalizarse antes de desplegar en producción. `open_library_key` tampoco tiene todavía una restricción `UNIQUE` en el script.

## Configuración y ejecución

1. Crear la base de datos ejecutando `database/scriptLibrarium.sql`.
2. Revisar las credenciales de `src/main/resources/application.properties`.
3. Ejecutar `./mvnw spring-boot:run`.
4. Usar el puerto `8080`, salvo que se modifique `server.port`.

No se deben versionar credenciales reales. La configuración local debe migrarse a variables de entorno o perfiles de Spring antes de publicar el proyecto.

## Próximos bloques técnicos

1. Completar autenticación JWT, BCrypt y control de acceso.
2. Crear DTOs de respuesta para no exponer entidades ni `passwordHash`.
3. Añadir creación de respuestas de foro y manejo centralizado de errores.
4. Normalizar tipos y nombres de fechas entre SQL y entidades.
5. Añadir pruebas unitarias y de integración.

La guía interna de ejecución está en [`PROCESOS.md`](PROCESOS.md). El seguimiento público de cada bloque se realiza mediante GitHub Issues.
