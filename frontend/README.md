# Librarium Marginalia — frontend

Frontend estático con HTML, CSS y JavaScript normal. No necesitas conocer React ni otro framework.

## Desarrollo

```bash
npm install
npm run dev
```

`config.js` usa `http://localhost:8080` en desarrollo y selecciona automáticamente el mismo origen (`''`) cuando el frontend se sirve en `librarium.emserv.ovh`:

```js
apiUrl: ''
```

Para publicar, ejecuta `npm run build` y sube la carpeta `dist/` a `https://librarium.emserv.ovh`. El icono de Librarium está en `public/media/`; las portadas se cargan únicamente desde los datos de cada libro.

No abras `index.html` directamente con doble clic (`file://`). El navegador puede bloquear las
peticiones a la API por seguridad. En local usa siempre `npm run dev` y abre la dirección que
muestre Vite, normalmente `http://localhost:5173`.
