// `import.meta.env` is injected by Vite, but IntelliJ's static preview serves
// this module without it. Optional chaining keeps that preview from crashing
// before the app can render.
const viteApiUrl = import.meta.env?.VITE_API_URL
const isViteDevelopment = import.meta.env?.DEV

export default {
  apiUrl: viteApiUrl || (isViteDevelopment || ['localhost', '127.0.0.1'].includes(window.location.hostname)
    ? 'http://localhost:8080'
    : 'https://librarium-api.emserv.ovh')
}
