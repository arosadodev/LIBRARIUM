# Guía técnica para agentes: Librarium Marginalia

## Contexto

El backend está en `src/main/java/ovh/emserv/librarium/` y usa Java 25 con Spring Boot 4.1.2, según `pom.xml`. No asumir Java 17, Spring Boot 3 ni un directorio `api/`.

## Reglas de arquitectura

- Mantener la separación `Controller -> Service -> Repository`.
- Los controladores deben llamar a servicios y no inyectar repositorios.
- Los servicios contienen la lógica de negocio y acceden a repositorios u otros servicios.
- Los repositorios deben ser interfaces JPA sin lógica de presentación.
- Las entidades deben usar Lombok (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`) y anotaciones JPA.
- Usar `@PrePersist` para fechas generadas por el servidor.
- Mantener los nombres de dominio y rutas en español: `Libro`, `Resena`, `ForoHilo`, `/api/libros`, `/api/resenas`.

## Seguridad

La base de datos contiene `password_hash` y los roles `ROLE_USER` y `ROLE_ADMIN`. Toda implementación de registro debe usar `BCryptPasswordEncoder`; nunca guardar contraseñas en texto plano.

El objetivo de seguridad es autenticación stateless con JWT y autorización por roles. Actualmente esta parte aún no está terminada: cualquier cambio que la implemente debe añadir configuración, filtro, servicio de tokens, carga de usuarios y endpoints de autenticación de forma coherente.

## DTOs y respuestas

- No devolver `Usuario` directamente desde la API, porque contiene `passwordHash`.
- Preferir DTOs de respuesta para todas las entidades con relaciones.
- Evitar ciclos JSON causados por asociaciones JPA.
- Validar las entradas con Jakarta Validation.
- No permitir que el cliente establezca fechas de creación ni campos calculados.

## Open Library

Open Library es la fuente externa para buscar libros. La implementación actual usa `java.net.http.HttpClient` en `OpenLibraryService`; no documentar `RestClient` como si ya estuviera en uso.

El flujo previsto es:

`Buscar en Open Library -> seleccionar resultado -> persistir/asociar el libro localmente`

Debe evitarse duplicar libros mediante `open_library_key` cuando se normalice el esquema.

## Base de datos

El contrato de datos es `database/scriptLibrarium.sql`. Sus tablas principales son:

```text
usuarios
libros
resenas
anotaciones_guias
foro_hilos
foro_respuestas
autores
libros_autores
```

El script usa `BIGINT AUTO_INCREMENT`, compatible con `Long` en las entidades. Los nombres de fecha no son uniformes:

- `fecha_registro`: `usuarios`, `libros`, `resenas`.
- `fecha_creacion`: `anotaciones_guias`, `foro_hilos`, `foro_respuestas`.

No inventar nombres alternativos sin actualizar conjuntamente el script SQL, las entidades y la documentación.

## Validación de cambios

- Usar `./mvnw` para compilar y ejecutar pruebas.
- El entorno de compilación debe disponer de un JDK compatible con Java 25.
- Revisar `application.properties` antes de iniciar la aplicación.
- No introducir secretos en código, documentación ni archivos versionados.
