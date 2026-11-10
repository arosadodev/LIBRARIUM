# Procesos internos de trabajo

Guía personal para desarrollar y entregar Librarium Marginalia. Las tareas públicas y su seguimiento están en GitHub Issues; este archivo explica cómo ejecutar cada bloque.

## Rutina para cada tarea

1. Revisar la issue y sus dependencias.
2. Crear una rama descriptiva desde `master`, por ejemplo `feat/jwt-auth`.
3. Leer `README.md`, `AGENTS.md` y los archivos relacionados antes de editar.
4. Implementar el cambio completo respetando `Controller -> Service -> Repository`.
5. Ejecutar el formateo y las pruebas más pequeñas que cubran el cambio.
6. Revisar el diff y comprobar que no haya credenciales, contraseñas ni tokens.
7. Actualizar la documentación si cambia el comportamiento de la API.
8. Marcar la issue como resuelta mediante un commit o pull request.

## Orden de ejecución

No empezar un bloque hasta terminar sus dependencias:

`Entorno -> Base de datos -> Seguridad -> Catálogo -> Contenido -> Errores -> Pruebas -> Entrega`

| Orden | Bloque | GitHub Issue |
|---:|---|---|
| 1 | Preparar entorno | [#1](https://github.com/arosadodev/LIBRRARIUM/issues/1) |
| 2 | Normalizar base de datos | [#2](https://github.com/arosadodev/LIBRRARIUM/issues/2) |
| 3 | Implementar seguridad | [#3](https://github.com/arosadodev/LIBRRARIUM/issues/3) |
| 4 | Completar Open Library | [#4](https://github.com/arosadodev/LIBRRARIUM/issues/4) |
| 5 | Cerrar reseñas y anotaciones | [#5](https://github.com/arosadodev/LIBRRARIUM/issues/5) |
| 6 | Cerrar foros | [#6](https://github.com/arosadodev/LIBRRARIUM/issues/6) |
| 7 | Errores y validación | [#7](https://github.com/arosadodev/LIBRRARIUM/issues/7) |
| 8 | Pruebas | [#8](https://github.com/arosadodev/LIBRRARIUM/issues/8) |
| 9 | Preparar entrega | [#9](https://github.com/arosadodev/LIBRRARIUM/issues/9) |

## Proceso 1: preparar el entorno

**Objetivo:** poder compilar y arrancar la API localmente.

1. Seleccionar un JDK compatible con Java 25.
2. Ejecutar `./mvnw -DskipTests compile`.
3. Crear MySQL con `database/scriptLibrarium.sql`.
4. Configurar la conexión mediante variables de entorno o un perfil local no versionado.
5. Arrancar con `./mvnw spring-boot:run`.
6. Confirmar que no haya dos aplicaciones principales compitiendo por el arranque.

**Comprobación:** la aplicación inicia en el puerto configurado y conecta con MySQL.

## Proceso 2: normalizar la base de datos

**Objetivo:** que SQL y JPA tengan el mismo contrato.

1. Elegir `INT` o `BIGINT` para los identificadores.
2. Unificar `fecha_registro` y `fecha_creacion`.
3. Añadir unicidad para `open_library_key`.
4. Añadir la restricción de puntuación 1-5.
5. Revisar índices y claves foráneas.
6. Regenerar las inserciones de ejemplo con hashes BCrypt.
7. Crear una base de datos limpia y ejecutar el script desde cero.

**Comprobación:** el esquema recién creado permite iniciar Hibernate sin cambios inesperados.

## Proceso 3: implementar seguridad

**Objetivo:** proteger la API y gestionar usuarios correctamente.

1. Mantener la configuración de Spring Security.
2. Mantener registro, login y DTO de respuesta.
3. Hashear contraseñas con BCrypt antes de persistirlas.
4. Configurar `LIBRARIUM_JWT_SECRET` con una clave Base64 de al menos 32 bytes.
5. Configurar sesiones stateless.
6. Definir permisos para `ROLE_USER` y `ROLE_ADMIN`.
7. Probar respuestas 401 y 403.
8. Revisar que ningún endpoint devuelva `passwordHash`.

**Comprobación:** registro y login entregan un token válido; los recursos protegidos rechazan peticiones sin permisos.

## Proceso 4: completar Open Library

**Objetivo:** buscar externamente y persistir solo libros seleccionados.

1. Definir DTOs para la respuesta de Open Library.
2. Configurar un cliente HTTP reutilizable con timeout.
3. Implementar búsqueda por título, autor e ISBN.
4. Implementar selección y guardado local por `open_library_key`.
5. Reutilizar libros ya existentes.
6. Convertir errores externos en respuestas HTTP controladas.

**Comprobación:** buscar no inserta libros; seleccionar uno permite asociarlo a contenido local.

## Proceso 5: cerrar reseñas y anotaciones

**Objetivo:** ofrecer contenido validado y seguro.

1. Crear DTOs de salida sin relaciones recursivas ni datos sensibles.
2. Validar puntuación, contenido, libro y usuario.
3. Añadir consultas por libro y usuario.
4. Decidir la regla de reseñas duplicadas por usuario/libro.
5. Implementar edición y borrado para propietario o administrador.
6. Ignorar fechas y relaciones enviadas por el cliente.

**Comprobación:** las operaciones CRUD devuelven JSON estable y respetan permisos.

## Proceso 6: cerrar foros

**Objetivo:** completar la conversación por hilos.

1. Crear DTO de respuesta de foro.
2. Implementar `POST /api/foros/{hiloId}/respuestas`.
3. Validar hilo y usuario antes de guardar.
4. Añadir paginación.
5. Implementar edición y borrado autorizado.
6. Verificar que la serialización no produce ciclos.

**Comprobación:** un usuario autenticado puede crear hilos y respuestas y consultar la conversación completa.

## Proceso 7: errores y validación

**Objetivo:** mantener un contrato HTTP predecible.

1. Crear `@RestControllerAdvice`.
2. Definir JSON común para 400, 401, 403, 404 y 409.
3. Mantener `RecursoNoEncontradoException`, `PeticionInvalidaException`, `ConflictoException`, `AccesoDenegadoException` y `ServicioExternoException` según el caso.
4. Validar longitudes, campos obligatorios y enums.
5. Registrar errores sin incluir secretos.

**Comprobación:** cada error esperado tiene código HTTP, mensaje seguro y formato consistente.

## Proceso 8: pruebas

**Objetivo:** detectar regresiones antes de integrar cambios.

1. Escribir tests unitarios de servicios y excepciones de dominio.
2. Escribir tests de repositorios.
3. Escribir tests de controladores con `MockMvc`.
4. Probar registro, login, expiración y rechazo de JWT.
5. Probar roles, validaciones, duplicados y errores de Open Library.
6. Ejecutar `./mvnw test`.

**Comprobación:** la suite pasa desde un entorno limpio y cubre los flujos principales.

## Proceso 9: preparar la entrega

**Objetivo:** dejar el proyecto reproducible y publicable.

1. Documentar variables de entorno y perfiles `dev`/`test`.
2. Documentar endpoints y ejemplos JSON.
3. Mantener la documentación OpenAPI/Swagger UI y añadir ejemplos de seguridad.
4. Revisar `README.md` y `AGENTS.md`.
5. Configurar CI para compilar y ejecutar tests; el workflow está en `.github/workflows/ci.yml`.
6. Revisar secretos, logs, dependencias y diferencias del esquema.
7. Abrir pull request y enlazar las issues resueltas.

**Comprobación:** otra persona puede clonar, configurar y validar el proyecto siguiendo la documentación.
