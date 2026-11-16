# Librarium Marginalia — frontend

Frontend estático con HTML, CSS y JavaScript normal. No necesitas conocer React ni otro framework.

## Desarrollo

```bash
npm install
npm run dev
```

`config.js` usa `http://localhost:8080` en desarrollo. En producción el frontend y la API tienen orígenes separados; el build del frontend recibe la URL pública con `VITE_API_URL` (por ejemplo `https://librarium-api.emserv.ovh`). Si no se define en el build, se usa ese dominio como valor de producción predeterminado.

```js
apiUrl: import.meta.env?.VITE_API_URL
```

Para publicar como sitio estático, ejecuta `VITE_API_URL=https://librarium-api.emserv.ovh npm run build` y sube la carpeta `dist/` a `https://librarium.emserv.ovh`. En el despliegue Docker recomendado, configura `VITE_API_URL` en `.env` y usa Compose desde la raíz del proyecto. Si cambias el dominio de la API, actualiza también `connect-src` en `frontend/nginx.conf` y el origen permitido en `LIBRARIUM_CORS_ALLOWED_ORIGINS`.

El frontend Docker escucha en el puerto 8080 del contenedor (publicado como `127.0.0.1:8082` por Compose). El proxy HTTPS debe enviar el dominio del frontend a ese puerto. La API se publica localmente en `127.0.0.1:8081`; no expongas directamente el puerto de MySQL.

El host `librarium-api.emserv.ovh` debe llegar al contenedor Spring Boot y aceptar `GET`, `POST`, `PUT`, `PATCH`, `DELETE` y `OPTIONS`. Si login o registro devuelven 405, el proxy está rechazando el método o la petición está llegando al servidor estático del frontend. Después de cambiar `VITE_API_URL`, reconstruye la imagen frontend: Vite incorpora esa URL durante el build.

La autenticación usa cookies `HttpOnly` emitidas por la API y peticiones con `credentials: include`. El frontend no recibe ni guarda el JWT; la API exige CSRF para operaciones que modifican datos y CORS limita el origen al frontend. El cliente obtiene el token de `/api/auth/csrf` al arrancar; si la API rechaza un token caducado, lo renueva y reintenta una vez. Spring invalida el token CSRF al iniciar y cerrar sesión, por lo que el cliente también descarta su copia local en esos casos.

No abras `index.html` directamente con doble clic (`file://`). El navegador puede bloquear las
peticiones a la API por seguridad. En local usa siempre `npm run dev` y abre la dirección que
muestre Vite, normalmente `http://localhost:5173`.
