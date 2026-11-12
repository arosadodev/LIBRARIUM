const productionHosts = ['librarium.emserv.ovh', 'www.librarium.emserv.ovh']

export default {
  apiUrl: productionHosts.includes(window.location.hostname) ? '' : 'http://localhost:8080'
}
