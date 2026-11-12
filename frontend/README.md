# Librarium Marginalia — frontend

Frontend estático con HTML, CSS y JavaScript normal. No necesitas conocer React ni otro framework.

## Desarrollo

```bash
npm install
npm run dev
```

`config.js` usa `http://localhost:8080` en desarrollo. En producción el frontend y la API tienen orígenes separados; el build del frontend recibe la URL pública con `VITE_API_URL` (por ejemplo `https://librarium-api.emserv.ovh`):

```js
apiUrl: import.meta.env.VITE_API_URL
```

Para publicar, ejecuta `VITE_API_URL=https://librarium-api.emserv.ovh npm run build` y sube la carpeta `dist/` a `https://librarium.emserv.ovh`. El icono de Librarium está en `public/media/`; las portadas se aceptan desde el propio frontend o desde `covers.openlibrary.org`.

La autenticación usa cookies `HttpOnly` emitidas por la API y peticiones con `credentials: include`. El frontend no recibe ni guarda el JWT; la API exige CSRF para operaciones que modifican datos y CORS limita el origen al frontend.

No abras `index.html` directamente con doble clic (`file://`). El navegador puede bloquear las
peticiones a la API por seguridad. En local usa siempre `npm run dev` y abre la dirección que
muestre Vite, normalmente `http://localhost:5173`.
