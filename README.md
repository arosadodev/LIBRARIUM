# Librarium Marginalia

Librarium Marginalia es un backend en Spring Boot para una red social de lectura: catálogo de libros, reseñas, anotaciones por capítulo y debates en foros.

Este repositorio documenta el proyecto tal como está implementado en la actualidad y sirve como base para las siguientes fases de desarrollo.

## Estado actual del backend

- El paquete base es `ovh.emserv.librarium`.
- La aplicación principal es `ovh.emserv.librarium.LibrariumApplication`.
- Java 25 y Spring Boot 4.1.2 son los entornos objetivos reales.
- El proyecto compila con `./mvnw compile` y pasa `./mvnw test`.
- La base de datos objetivo es MySQL sobre `localhost:3308`, con esquema relacional para libros, usuarios, reseñas, anotaciones y foros.
- La autenticación usa JWT firmado en una cookie `HttpOnly`, BCrypt, protección CSRF y autorización basada en roles (`ROLE_USER` y `ROLE_ADMIN`). JavaScript no recibe ni almacena el JWT.
- La clave JWT se carga desde la propiedad obligatoria `librarium.jwt.secret` (o variable de entorno `LIBRARIUM_JWT_SECRET`). Nunca se almacena en el repositorio.
- En producción, la clave de firma debe persistir en el archivo de secretos protegido del servidor (o en el `.env` privado de Compose) y mantenerse igual entre reinicios y despliegues. El JWT de sesión caduca a las 24 horas; no conviene usar tokens de acceso sin caducidad.
- Frontend y API se despliegan por separado: frontend en `librarium.emserv.ovh`, API en `librarium-api.emserv.ovh`. CORS solo permite el origen del frontend.
- Se integra con la API pública de Open Library para buscar libros e importar datos mínimos al catálogo local (`open_library_key`, título, ISBN y año).
- La documentación interactiva OpenAPI/Swagger UI se expone en `/swagger-ui/index.html`.

## Estructura principal del código

```text
src/main/java/ovh/emserv/librarium/
├── controller/     Controladores REST
├── dto/            Objetos de transferencia y validación de entrada/salida
├── entity/         Entidades JPA
├── exception/      Excepciones de negocio y manejador global
├── repository/     Interfaces Spring Data JPA
├── service/        Lógica de negocio y casos de uso
└── LibrariumApplication.java
```

## Entidades y relaciones

- `Libro`: datos del catálogo y clave única de Open Library cuando proceda.
- `Usuario`: credenciales, datos de perfil y rol (`ROLE_USER`, `ROLE_ADMIN`).
- `Resena`: valoración (1-5) y texto asociado a un libro y un usuario. Un usuario solo puede tener una reseña por libro.
- `Anotacion`: cita, resumen o reflexión vinculada a una página o capítulo de un libro.
- `ForoHilo`: tema de debate vinculado a una obra.
- `ForoRespuesta`: intervención en un hilo.

## Puntos de entrada de la API

La API expone los siguientes recursos principales bajo `/api`:

- `/api/auth/register`: registro de usuarios con hash BCrypt.
- `/api/auth/login`: autenticación y emisión de token JWT.
- `/api/libros`: catálogo local (búsqueda por título, ISBN y listado).
- `/api/libros/open-library/buscar`: consulta en tiempo real a Open Library.
- `/api/libros/open-library/importar`: importación de obras externas al catálogo local.
- `/api/resenas`: creación, edición, borrado y consulta de reseñas.
- `/api/anotaciones`: gestión de citas y notas de lectura.
- `/api/foros/hilos` y `/api/foros/respuestas`: hilos y respuestas de discusión.
- `/api/usuarios/me` y `/api/usuarios/{username}`: perfil propio y perfiles públicos.

## Requisitos de ejecución

- Java 25.
- Docker o MySQL 8.x / 9.x local disponible en el puerto `3308`.
- Clave de firma JWT en Base64 de al menos 32 bytes (`LIBRARIUM_JWT_SECRET`).

## Puesta en marcha

1. Clonar el repositorio.
2. Crear la base de datos `librarium_db` con `database/scriptLibrarium.sql`.
3. Para pruebas locales, cargar datos iniciales con `database/insertions.sql`.
4. Configurar la clave JWT y las credenciales de MySQL mediante variables de entorno antes de iniciar:

   ```bash
   export LIBRARIUM_JWT_SECRET="$(openssl rand -base64 48)"
   export SPRING_DATASOURCE_USERNAME="tu_usuario_mysql"
   export SPRING_DATASOURCE_PASSWORD="tu_contrasena_mysql"
   ```

5. Usar el puerto `8080`, salvo que se modifique `server.port`.

No se deben versionar credenciales reales. La configuración local debe migrarse a variables de entorno o perfiles de Spring antes de publicar el proyecto.

Para Docker de producción, copia `.env.example` a `.env`, cambia todos los marcadores por valores aleatorios y conserva ese `.env` solo en el servidor con permisos restringidos (`chmod 600 .env`). Puedes generar contraseñas MySQL con `openssl rand -hex 32` y la clave JWT con `openssl rand -base64 48`. El `.env` está excluido de Git y de las imágenes; no lo regeneres en cada despliegue. En un volumen MySQL ya inicializado, cambiar `MYSQL_PASSWORD` en `.env` no cambia la contraseña guardada dentro de MySQL; hay que actualizar el usuario también en el servidor y reiniciar la API con el mismo valor.

### Ejecución local con Docker

Construir la imagen del backend desde la raíz del proyecto:

```bash
docker build -t librarium-api .
```

Para el despliegue separado con Compose, configura `.env` en el servidor y arranca los tres servicios:

```bash
docker compose --env-file .env -f docker-compose.prod.yml up -d --build
```

El proxy HTTPS del servidor debe enrutar `librarium.emserv.ovh` a `127.0.0.1:8082` y `librarium-api.emserv.ovh` a `127.0.0.1:8081`. Mantén la base de datos sin puertos publicados. Si cambias `VITE_API_URL`, ajusta también `connect-src` en `frontend/nginx.conf` y el origen permitido en CORS.

`mysql` debe ser el nombre o alias de red del contenedor MySQL. No se debe copiar `.env` dentro de la imagen ni publicar secretos en el repositorio.

Para el despliegue de producción existe una guía operativa separada y privada, fuera del contenido público del repositorio. La carpeta `deploy/` está ignorada por Git para evitar publicar infraestructura, dominios internos o variables de operación.

## Próximos bloques técnicos

1. Añadir más pruebas de integración MockMvc y repositorios.
2. Versionar las migraciones de base de datos.
3. Completar ejemplos de permisos y errores en OpenAPI.

El seguimiento de cada bloque se realiza mediante los hitos y tareas del proyecto.
