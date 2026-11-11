# Administración de Librarium

## Crear el primer administrador

El frontend **no ofrece ninguna opción para crear administradores**. El registro público (`POST /api/auth/register`) asigna siempre `ROLE_USER`, y el DTO de registro no acepta un rol.

1. Arranca MySQL y la API.
2. Registra el usuario normalmente desde Librarium o con la API:

   ```http
   POST https://api.librarium.emserv.ovh/api/auth/register
   Content-Type: application/json

   {
     "username": "admin",
     "email": "admin@tu-dominio.example",
     "password": "CAMBIAR_INMEDIATAMENTE"
   }
   ```

3. Conéctate a la base de datos con una cuenta administrativa y cambia únicamente el rol:

   ```sql
   UPDATE usuarios
   SET rol = 'ROLE_ADMIN'
   WHERE email = 'admin@tu-dominio.example';
   ```

4. Cierra cualquier sesión previa e inicia sesión otra vez para recibir un JWT con `ROLE_ADMIN`.

La contraseña se guarda mediante BCrypt durante el registro. No se debe insertar una contraseña plana directamente en SQL ni publicar credenciales. Protege el acceso a MySQL y utiliza una contraseña larga y única.

## Permisos

Los administradores pueden gestionar contenido protegido cuando el servicio comprueba el rol: reseñas, anotaciones, hilos y respuestas. El frontend no permite seleccionar ni editar roles; la autorización real siempre se aplica en el backend.
