# Estado actual de Librarium Marginalia

Este documento describe lo que hace el proyecto **ahora mismo**, cómo se ejecuta cada flujo y qué partes siguen pendientes.

## 1. Qué es el proyecto

Librarium Marginalia es una API REST para:

- consultar libros locales;
- buscar libros en Open Library;
- importar un libro seleccionado;
- registrar usuarios e iniciar sesión;
- crear, consultar, editar y borrar reseñas;
- crear, consultar, editar y borrar anotaciones;
- crear, consultar, editar y borrar hilos de foro y sus respuestas.

La aplicación está implementada con:

- Java 25;
- Spring Boot 4.1.2;
- Spring MVC;
- Spring Data JPA/Hibernate;
- MySQL;
- Spring Security;
- JWT con clave persistente configurada mediante `LIBRARIUM_JWT_SECRET`;
- BCrypt;
- Maven mediante `./mvnw`.

El código real está en `src/main/java/ovh/emserv/librarium/`.

## 2. Cómo arranca la aplicación

1. Maven compila el código usando la versión de Java declarada en `pom.xml`.
2. Spring Boot inicia `LibrariumApplication`.
3. Spring descubre los controladores, servicios, repositorios y entidades.
4. Hibernate conecta con MySQL y valida/actualiza el esquema según:

   ```properties
   spring.jpa.hibernate.ddl-auto=update
   ```

5. La API queda disponible normalmente en:

   ```text
   http://localhost:8080
   ```

Para arrancar:

```bash
./mvnw spring-boot:run
```

La configuración actual usa MySQL en el puerto `3308`, la base `librarium_db` y el usuario `root`. La contraseña debe configurarse localmente y no debe publicarse.

## 3. Base de datos

El esquema se crea desde:

```text
database/scriptLibrarium.sql
```

Si la base ya existía antes del cambio de `INT` a `BIGINT`, hay que ejecutar
`database/migrate-int-to-bigint.sql` con una copia de seguridad previa. Hibernate
no puede modificar las columnas referenciadas mientras sus claves foráneas siguen
activas.

Contiene estas tablas:

1. `usuarios`
2. `libros`
3. `autores`
4. `libros_autores`, tabla puente que permite varios autores por libro
5. `resenas`
6. `anotaciones_guias`
7. `foro_hilos`
8. `foro_respuestas`

Los identificadores SQL usan `BIGINT`, que corresponde con `Long` en las entidades Java. Un libro
se relaciona con uno o varios registros de `autores` mediante `libros_autores`; ya no se guarda
un único autor como texto dentro de `libros`.

### Datos de ejemplo

Después de crear el esquema se pueden cargar datos con:

```text
database/insertions.sql
```

Las contraseñas de ejemplo están almacenadas como hashes BCrypt. No se guardan contraseñas planas.

## 4. Arquitectura de cada petición

El flujo general es:

```text
Cliente HTTP
    -> Controller
    -> Service
    -> Repository
    -> MySQL
```

- **Controller:** recibe la petición, valida el cuerpo y devuelve la respuesta HTTP.
- **Service:** contiene la lógica de negocio.
- **Repository:** consulta y guarda entidades mediante JPA.
- **Entity:** representa una tabla de la base de datos.
- **DTO:** representa los datos de entrada o salida que no deben coincidir necesariamente con una entidad.

## 5. Seguridad actual

### Registro

Petición:

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "username": "andres",
  "email": "andres@gmail.com",
  "password": "1234"
}
```

El flujo es:

1. `AutenticacionController` recibe y valida el DTO.
2. Comprueba si el email ya existe.
3. Codifica la contraseña con `BCryptPasswordEncoder`.
4. Guarda el usuario con rol `ROLE_USER`.
5. Devuelve `201 Created`.

### Login

Petición:

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "andres@gmail.com",
  "password": "1234"
}
```

El flujo es:

1. `AuthenticationManager` busca el usuario por email.
2. BCrypt compara la contraseña recibida con `password_hash`.
3. `ServicioJwt` crea un JWT con el email, la fecha de emisión, la expiración y los roles.
4. La respuesta contiene el token.

Para acceder a recursos protegidos:

```http
Authorization: Bearer <token>
```

### Reglas actuales

- `/api/auth/**` es público.
- Las peticiones `GET /api/libros/**` son públicas.
- La importación de libros requiere autenticación.
- Reseñas, anotaciones y foros requieren autenticación.
- La sesión es stateless.
- Las autoridades se leen desde el claim `roles`.
- `Usuario` implementa `UserDetails`.

### Limitación importante

`ServicioJwt` carga una clave Base64 persistente desde `LIBRARIUM_JWT_SECRET`. La aplicación no debe arrancarse sin esa variable en entornos reales.

El registro público solo crea usuarios `ROLE_USER`. La promoción controlada a `ROLE_ADMIN` se realiza directamente en MySQL y está documentada en `admin.md`; el frontend no puede crear administradores.

## 6. Libros locales

### Listar libros

```http
GET /api/libros
```

Devuelve los libros guardados en MySQL.

### Obtener un libro

```http
GET /api/libros/{id}
```

### Buscar localmente

```http
GET /api/libros/buscar?q=orwell
```

Busca por título o autor.

## 7. Open Library

### Buscar externamente

```http
GET /api/libros/buscar-externo?q=1984
```

También se pueden usar consultas de autor o ISBN:

```http
GET /api/libros/buscar-externo?q=author:George Orwell
GET /api/libros/buscar-externo?q=isbn:9780451524935
```

El flujo es:

1. El controlador recibe `q`.
2. `OpenLibraryService` valida que no esté vacío.
3. Codifica la consulta para URL.
4. Envía una petición HTTP a `https://openlibrary.org/search.json`.
5. Aplica timeout de conexión de 5 segundos.
6. Aplica timeout de petición de 10 segundos.
7. Convierte la respuesta JSON en `OpenLibraryLibroDTO`.
8. Devuelve los resultados.

La búsqueda **no guarda libros**.

### Importar un resultado seleccionado

```http
POST /api/libros/importar
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "key": "/works/OL27448W",
  "title": "1984",
  "authors": ["George Orwell"],
  "isbn": "9780451524935",
  "firstPublishYear": 1949,
  "coverUrl": "https://covers.openlibrary.org/b/id/7222246-L.jpg"
}
```

El flujo es:

1. El usuario busca resultados.
2. El cliente elige un resultado.
3. El cliente envía sus datos al endpoint de importación.
4. `LibroRepository` busca por `open_library_key`.
5. Si existe, se devuelve el libro existente.
6. Si no existe, se crea y guarda un libro local.

La clave externa es única para evitar duplicados.

## 8. Reseñas

### Crear reseña

```http
POST /api/resenas
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "puntuacion": 5,
  "comentario": "Excelente lectura.",
  "libroId": 1,
  "usuarioId": 2
}
```

La puntuación se valida entre 1 y 5 mediante Jakarta Validation y la restricción de base de datos.

La API devuelve DTOs de salida y ofrece consulta, edición y borrado con permisos de propietario o administrador. La base de datos mantiene una reseña única por usuario y libro.

## 9. Anotaciones

### Consultar anotaciones

```http
GET /api/anotaciones
GET /api/anotaciones?libroId=1
```

### Crear anotación

```http
POST /api/anotaciones
Authorization: Bearer <token>
Content-Type: application/json
```

El servicio valida y relaciona el libro y el usuario antes de guardar:

```text
capituloNumero
pagina
tipo
contenido
libroId
usuarioId
```

La API ofrece edición y borrado con permisos de propietario o administrador y devuelve DTOs sin datos sensibles.

## 10. Foros

### Consultar hilos

```http
GET /api/foros
GET /api/foros?libroId=1
```

### Crear hilo

```http
POST /api/foros
Authorization: Bearer <token>
Content-Type: application/json
```

El hilo se relaciona con un libro y un usuario existentes.

### Consultar respuestas

```http
GET /api/foros/{hiloId}/respuestas?page=0&size=20
```

Se pueden consultar hilos y respuestas paginadas, crear respuestas, editar y borrar hilos/respuestas para el propietario o administradores.

## 11. Estado de entrega

Las issues funcionales #5–#9 están cerradas. La entrega actual incluye:

1. **Reseñas, anotaciones y foros:** CRUD, DTOs, permisos de propietario/admin y paginación.
2. **Errores HTTP:** `ManejoErrores` cubre validación, petición inválida, conflicto, acceso denegado, recurso no encontrado, integridad y Open Library.
3. **Pruebas automatizadas:** pruebas unitarias y de contexto con H2; la suite `./mvnw test` pasa sin depender de MySQL.
4. **CI:** workflow reproducible en `.github/workflows/ci.yml` con Java 25.

## 12. Deuda técnica priorizada

1. Añadir más pruebas MockMvc de 401/403 y CRUD.
2. Versionar migraciones de base de datos.
3. Completar ejemplos OpenAPI y variables de entorno.

El manejo global de errores devuelve JSON uniforme para validaciones, recursos inexistentes,
peticiones inválidas, conflictos de negocio o integridad, acceso denegado y fallos de Open Library.

## 13. Comprobación mínima manual

Para comprobar el flujo principal:

1. Arrancar MySQL.
2. Crear el esquema con `database/scriptLibrarium.sql`.
3. Arrancar la API:

   ```bash
   ./mvnw spring-boot:run
   ```

4. Registrar un usuario.
5. Iniciar sesión y copiar el JWT.
6. Buscar un libro en Open Library.
7. Importarlo usando el JWT.
8. Repetir la importación y comprobar que no se duplica.
9. Crear una reseña, una anotación y un hilo usando el mismo JWT.
10. Consultar los recursos creados.

En producción, el frontend se sirve desde `https://librarium.emserv.ovh` y consume la API en `https://api.librarium.emserv.ovh`.
