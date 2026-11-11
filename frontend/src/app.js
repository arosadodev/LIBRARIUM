import config from '../config.js'

const API_URL = config.apiUrl.replace(/\/$/, '')
const fallbackCover = '/media/librarium-icon.svg'
const app = document.querySelector('#app')
const modal = document.querySelector('#modal')
let books = []
let currentBook = null
let currentPage = 'inicio'

const escapeHtml = (value = '') => String(value).replace(/[&<>"']/g, (character) => ({
  '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;'
}[character]))

async function request(path, options = {}) {
  const token = localStorage.getItem('librarium_token')
  const headers = {
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers
  }
  const response = await fetch(`${API_URL}${path}`, { ...options, headers })
  if (!response.ok) {
    const error = await response.json().catch(() => ({}))
    throw new Error(error.message || error.error || `La API respondió con el estado ${response.status}.`)
  }
  return response.status === 204 ? null : response.json()
}

function isLoggedIn() { return Boolean(localStorage.getItem('librarium_token')) }
function requireLogin() {
  if (!isLoggedIn()) {
    showAuth('login')
    showToast('Inicia sesión para continuar.')
    return false
  }
  return true
}
function showToast(message) {
  const toast = document.querySelector('#toast')
  toast.textContent = message
  toast.hidden = false
  window.clearTimeout(showToast.timeout)
  showToast.timeout = window.setTimeout(() => { toast.hidden = true }, 4500)
}
function title(book) { return book?.titulo || book?.title || 'Sin título' }
function authors(book) {
  if (Array.isArray(book?.autores)) return book.autores.map((item) => item.nombre || item.name).join(', ') || 'Autor desconocido'
  if (Array.isArray(book?.authors)) return book.authors.join(', ') || 'Autor desconocido'
  return book?.author || 'Autor desconocido'
}
function year(book) { return book?.anoPublicacion || book?.firstPublishYear || '—' }
function cover(book) { return book?.portadaUrl || book?.coverUrl || fallbackCover }
function bookId(book) { return book?.id ?? book?.key }
function getFavorites() { return JSON.parse(localStorage.getItem('librarium_favorites') || '[]') }
function isFavorite(book) { return getFavorites().some((item) => String(bookId(item)) === String(bookId(book))) }
function toggleFavorite(book) {
  const saved = getFavorites()
  const next = isFavorite(book) ? saved.filter((item) => String(bookId(item)) !== String(bookId(book))) : [...saved, book]
  localStorage.setItem('librarium_favorites', JSON.stringify(next))
  showToast(isFavorite(book) ? 'Libro eliminado de tus lecturas.' : 'Libro guardado en tus lecturas.')
}

async function loadBooks(search = '') {
  try {
    books = await request(search ? `/api/libros/buscar?q=${encodeURIComponent(search)}` : '/api/libros')
    return books
  } catch (error) {
    showToast(`No se puede cargar el catálogo. ${error.message}`)
    return []
  }
}

function searchForm() {
  return `<form class="search-box" id="search-form"><span aria-hidden="true">⌕</span><input name="q" placeholder="Busca por título, autor o ISBN..." required><button class="primary-button">Buscar</button></form>`
}
function bookGrid(items) {
  if (!items.length) return '<div class="empty"><h3>Aún no hay libros aquí</h3><p>Prueba otra búsqueda o vuelve más tarde.</p></div>'
  return items.map((book) => `<article class="book-card">
    <button class="cover-wrap" data-book="${escapeHtml(bookId(book))}" aria-label="Ver ficha de ${escapeHtml(title(book))}"><img src="${escapeHtml(cover(book))}" alt="Portada de ${escapeHtml(title(book))}"><span>Ver ficha →</span></button>
    <button class="save-book ${isFavorite(book) ? 'saved' : ''}" data-save="${escapeHtml(bookId(book))}" aria-label="Guardar ${escapeHtml(title(book))}">♡</button>
    <p class="book-author">${escapeHtml(authors(book))}</p><h3>${escapeHtml(title(book))}</h3><p class="book-year">${escapeHtml(year(book))}</p>
  </article>`).join('')
}
function bindBookActions() {
  document.querySelectorAll('[data-book]').forEach((element) => element.addEventListener('click', () => {
    currentBook = books.find((book) => String(bookId(book)) === element.dataset.book) || getFavorites().find((book) => String(bookId(book)) === element.dataset.book)
    if (currentBook) showPage('detalle')
  }))
  document.querySelectorAll('[data-save]').forEach((element) => element.addEventListener('click', (event) => {
    event.stopPropagation()
    const book = books.find((item) => String(bookId(item)) === element.dataset.save) || getFavorites().find((item) => String(bookId(item)) === element.dataset.save)
    if (book) { toggleFavorite(book); if (currentPage === 'favoritos') renderFavorites() }
  }))
}
function bindSearch() {
  const form = document.querySelector('#search-form')
  if (!form) return
  form.addEventListener('submit', async (event) => {
    event.preventDefault()
    const query = new FormData(form).get('q')
    showPage('catalogo')
    const input = document.querySelector('#search-form input')
    if (input) input.value = query
    const items = await loadBooks(query)
    const target = document.querySelector('#catalog-books')
    if (target) { document.querySelector('#result-label').textContent = `${items.length} resultados`; target.innerHTML = bookGrid(items); bindBookActions() }
  })
}
function bindNavigation() {
  document.querySelectorAll('[data-go]').forEach((element) => element.addEventListener('click', () => showPage(element.dataset.go)))
}

function renderHome() {
  app.innerHTML = `<section class="hero"><div class="hero-copy"><p class="eyebrow">✦ Tu biblioteca, con margen para pensar</p><h1>Historias que<br><em>dejan huella.</em></h1><p class="hero-text">Descubre libros, guarda tus hallazgos y comparte las ideas que aparecen entre líneas.</p>${searchForm()}<button class="text-button" data-go="catalogo">Explorar catálogo →</button></div><div class="hero-art"><div class="sun"></div><div class="book-stack"></div><p class="hero-note">“Cada libro<br>es un viaje”</p></div></section><section class="section"><div class="section-heading"><div><p class="eyebrow">La estantería de hoy</p><h2>Lecturas que recomendamos</h2></div><button class="text-button" data-go="catalogo">Ver todo →</button></div><div id="home-books" class="book-grid"><p class="muted">Cargando la estantería...</p></div></section><section class="feature-strip"><div><p class="eyebrow">Más que leer</p><h2>Deja tu propia huella</h2><p>Publica una reseña, abre un hilo para conversar o guarda una cita que quieras recordar.</p></div><div class="feature-actions"><button class="primary-button" data-go="comunidad">Entrar en la comunidad</button><button class="outline-button" data-go="anotaciones">Guardar una cita</button></div></section>`
  bindNavigation(); bindSearch()
  loadBooks().then((items) => { const target = document.querySelector('#home-books'); if (target) { target.innerHTML = bookGrid(items.slice(0, 4)); bindBookActions() } })
}
function renderCatalog() {
  app.innerHTML = `<section class="section page"><div class="page-intro"><p class="eyebrow">▱ La biblioteca</p><h1>Encuentra tu próxima lectura</h1><p>Un catálogo para perderse un rato y salir con un libro bajo el brazo.</p></div>${searchForm()}<form class="external-search" id="external-search"><input name="q" required placeholder="¿No está? Busca en Open Library..."><button class="outline-button">Buscar fuera →</button></form><div id="external-results"></div><p id="result-label" class="result-label"></p><div id="catalog-books" class="book-grid"><p class="muted">Cargando libros...</p></div></section>`
  bindSearch()
  document.querySelector('#external-search').addEventListener('submit', searchExternalBooks)
  loadBooks().then((items) => { document.querySelector('#result-label').textContent = `${items.length} resultados`; document.querySelector('#catalog-books').innerHTML = bookGrid(items); bindBookActions() })
}
async function searchExternalBooks(event) {
  event.preventDefault()
  const query = new FormData(event.target).get('q')
  const target = document.querySelector('#external-results')
  target.innerHTML = '<p class="muted">Buscando en Open Library...</p>'
  try {
    const results = await request(`/api/libros/buscar-externo?q=${encodeURIComponent(query)}`)
    const externalBooks = results.slice(0, 6)
    target.innerHTML = externalBooks.length ? `<div class="external-grid">${externalBooks.map((book, index) => `<article class="external-card"><img src="${escapeHtml(book.coverUrl || book.portadaUrl || fallbackCover)}" alt="Portada de ${escapeHtml(book.title || book.titulo || 'Sin título')}"><div><h3>${escapeHtml(book.title || book.titulo || 'Sin título')}</h3><p>${escapeHtml((book.authors || book.autores || []).join(', ') || 'Autor desconocido')}</p><small>${escapeHtml(book.firstPublishYear || book.anoPublicacion || 'Año desconocido')}</small><button class="outline-button" data-import-index="${index}">Añadir al catálogo</button></div></article>`).join('')}</div>` : '<p class="muted">No hemos encontrado resultados externos.</p>'
    document.querySelectorAll('[data-import-index]').forEach((button) => button.addEventListener('click', () => importExternalBook(externalBooks[Number(button.dataset.importIndex)])))
  } catch (error) { target.innerHTML = `<p class="muted">${escapeHtml(error.message)}</p>` }
}
async function importExternalBook(book) {
  if (!requireLogin()) return
  try {
    await request('/api/libros/importar', { method: 'POST', body: JSON.stringify(book) })
    showToast('Libro añadido a tu catálogo.')
    document.querySelector('#external-results').innerHTML = ''
    const items = await loadBooks()
    document.querySelector('#result-label').textContent = `${items.length} resultados`
    document.querySelector('#catalog-books').innerHTML = bookGrid(items)
    bindBookActions()
  } catch (error) { showToast(error.message) }
}
function renderDiscover() {
  app.innerHTML = `<section class="section page"><div class="page-intro"><p class="eyebrow">✧ Una recomendación al azar</p><h1>Para leer sin prisa</h1><p>Pequeños desvíos de la ruta habitual. Quizá aquí encuentres tu próximo libro favorito.</p></div><div id="discover-books" class="book-grid"><p class="muted">Buscando historias...</p></div></section>`
  loadBooks().then((items) => { document.querySelector('#discover-books').innerHTML = bookGrid([...items].sort(() => Math.random() - .5)); bindBookActions() })
}
function renderFavorites() {
  app.innerHTML = `<section class="section page"><div class="page-intro"><p class="eyebrow">♡ Tu biblioteca personal</p><h1>Mis lecturas</h1><p>Los libros que has querido guardar para volver a ellos.</p></div>${bookGrid(getFavorites())}</section>`
  bindBookActions()
}
async function renderDetail() {
  if (!currentBook) return showPage('catalogo')
  const id = bookId(currentBook)
  app.innerHTML = `<section class="section page detail-page"><button class="back-button" data-go="catalogo">← Volver al catálogo</button><div class="detail-layout"><img class="detail-cover" src="${escapeHtml(cover(currentBook))}" alt="Portada de ${escapeHtml(title(currentBook))}"><div class="detail-copy"><p class="eyebrow">Ficha de lectura</p><h1>${escapeHtml(title(currentBook))}</h1><p class="detail-author">${escapeHtml(authors(currentBook))}</p><p class="detail-description">${escapeHtml(currentBook.sinopsis || 'Una historia esperando a ser descubierta. Lee, subraya y deja aquí tus impresiones.')}</p><p class="detail-meta"><b>${escapeHtml(year(currentBook))}</b> año</p><button class="outline-button" id="save-button">${isFavorite(currentBook) ? '♡ Guardado en mis lecturas' : '♡ Guardar en mis lecturas'}</button><button class="outline-button" data-go="anotaciones">✎ Añadir anotación</button></div></div><div class="reviews"><p class="eyebrow">La conversación</p><h2>Reseñas de lectores</h2><div id="reviews">Cargando reseñas...</div><form id="review-form" class="review-form"><h3>Deja tu impresión</h3><select name="puntuacion"><option value="5">★★★★★ · Imprescindible</option><option value="4">★★★★☆ · Muy recomendable</option><option value="3">★★★☆☆ · Interesante</option><option value="2">★★☆☆☆ · Irregular</option><option value="1">★☆☆☆☆ · No conecté</option></select><textarea name="comentario" maxlength="5000" placeholder="¿Qué te ha hecho sentir este libro?"></textarea><button class="primary-button">Publicar reseña →</button></form></div></section>`
  bindNavigation()
  document.querySelector('#save-button').addEventListener('click', () => { toggleFavorite(currentBook); renderDetail() })
  document.querySelector('#review-form').addEventListener('submit', submitReview)
  try {
    const reviews = await request(`/api/resenas?libroId=${id}`)
    document.querySelector('#reviews').innerHTML = reviews.length ? reviews.map(reviewHtml).join('') : '<p class="muted">Todavía no hay reseñas. Sé la primera persona en dejar una.</p>'
  } catch (error) { document.querySelector('#reviews').innerHTML = `<p class="muted">${escapeHtml(error.message)}</p>` }
}
function reviewHtml(item) { return `<article class="review"><div class="review-stars">${'★'.repeat(item.puntuacion || 0)}${'☆'.repeat(5 - (item.puntuacion || 0))}</div><p>${escapeHtml(item.comentario || 'Sin comentario')}</p><small>${escapeHtml(item.usuario?.username || 'Lector de Librarium')}</small></article>` }
async function submitReview(event) {
  event.preventDefault()
  if (!requireLogin()) return
  const data = Object.fromEntries(new FormData(event.target))
  data.puntuacion = Number(data.puntuacion); data.libroId = bookId(currentBook)
  try { await request('/api/resenas', { method: 'POST', body: JSON.stringify(data) }); showToast('Tu reseña se ha guardado.'); renderDetail() } catch (error) { showToast(error.message) }
}

async function renderCommunity() {
  app.innerHTML = `<section class="section page"><div class="page-intro"><p class="eyebrow">✦ La conversación</p><h1>Comunidad lectora</h1><p>Abre un hilo, comparte una idea y responde a otros lectores.</p></div><div class="community-layout"><div><div id="threads" class="thread-list"><p class="muted">Cargando conversaciones...</p></div></div><form id="thread-form" class="panel-form"><h2>Crear un blog</h2><p class="muted">Publica una conversación vinculada a un libro del catálogo.</p><input name="tituloHilo" maxlength="200" required placeholder="Título de tu hilo"><textarea name="contenidoInicial" maxlength="10000" required placeholder="¿Qué quieres compartir?"></textarea><select name="libroId" id="thread-book" required><option value="">Selecciona un libro</option></select><button class="primary-button">Publicar hilo →</button></form></div></section>`
  if (!isLoggedIn()) {
    document.querySelector('#threads').innerHTML = '<div class="empty"><h3>Inicia sesión para entrar en la comunidad</h3><p>Podrás leer, crear hilos y responder a otros lectores.</p><button class="primary-button" id="community-login">Iniciar sesión →</button></div>'
    document.querySelector('#community-login').addEventListener('click', () => showAuth('login'))
    document.querySelector('#thread-form').addEventListener('submit', (event) => { event.preventDefault(); showAuth('login') })
  }
  const items = await loadBooks()
  const select = document.querySelector('#thread-book')
  items.forEach((book) => { select.insertAdjacentHTML('beforeend', `<option value="${escapeHtml(book.id)}">${escapeHtml(title(book))}</option>`) })
  document.querySelector('#thread-form').addEventListener('submit', submitThread)
  if (!isLoggedIn()) return
  try {
    const threads = await request('/api/foros')
    document.querySelector('#threads').innerHTML = threads.length ? threads.map(threadHtml).join('') : '<div class="empty"><h3>Aún no hay conversaciones</h3><p>Empieza tú y crea el primer hilo.</p></div>'
    document.querySelectorAll('[data-thread]').forEach((element) => element.addEventListener('click', () => renderThread(Number(element.dataset.thread), element.dataset.title)))
  } catch (error) { document.querySelector('#threads').innerHTML = `<p class="muted">${escapeHtml(error.message)}</p>` }
}
function threadHtml(item) { return `<article class="thread-card"><div><p class="eyebrow">Hilo lector</p><h2>${escapeHtml(item.tituloHilo)}</h2><p>${escapeHtml(item.contenidoInicial)}</p><small>Por ${escapeHtml(item.usuario?.username || 'lector')} · ${escapeHtml(item.fechaCreacion || '')}</small></div><button class="outline-button" data-thread="${item.id}" data-title="${escapeHtml(item.tituloHilo)}">Ver respuestas →</button></article>` }
async function submitThread(event) {
  event.preventDefault()
  if (!requireLogin()) return
  try { await request('/api/foros', { method: 'POST', body: JSON.stringify(Object.fromEntries(new FormData(event.target))) }); showToast('Tu hilo se ha publicado.'); renderCommunity() } catch (error) { showToast(error.message) }
}
async function renderThread(id, threadTitle) {
  app.innerHTML = `<section class="section page"><button class="back-button" data-go="comunidad">← Volver a la comunidad</button><div class="page-intro"><p class="eyebrow">Conversación</p><h1>${escapeHtml(threadTitle)}</h1></div><div id="replies" class="reply-list">Cargando respuestas...</div><form id="reply-form" class="panel-form narrow"><textarea name="contenido" maxlength="10000" required placeholder="Escribe una respuesta..."></textarea><button class="primary-button">Responder →</button></form></section>`
  bindNavigation()
  try {
    const page = await request(`/api/foros/${id}/respuestas?page=0&size=50`)
    const replies = page.content || []
    document.querySelector('#replies').innerHTML = replies.length ? replies.map((item) => `<article class="reply"><p>${escapeHtml(item.contenido)}</p><small>${escapeHtml(item.usuario?.username || 'lector')} · ${escapeHtml(item.fechaCreacion || '')}</small></article>`).join('') : '<p class="muted">Todavía no hay respuestas.</p>'
  } catch (error) { document.querySelector('#replies').innerHTML = `<p class="muted">${escapeHtml(error.message)}</p>` }
  document.querySelector('#reply-form').addEventListener('submit', async (event) => {
    event.preventDefault(); if (!requireLogin()) return
    try { await request(`/api/foros/${id}/respuestas`, { method: 'POST', body: JSON.stringify(Object.fromEntries(new FormData(event.target))) }); showToast('Respuesta publicada.'); renderThread(id, threadTitle) } catch (error) { showToast(error.message) }
  })
}

async function renderAnnotations() {
  app.innerHTML = `<section class="section page"><div class="page-intro"><p class="eyebrow">✎ Marginalia</p><h1>Citas y anotaciones</h1><p>Guarda esas frases que quieres volver a encontrar, junto a tus análisis y referencias.</p></div><div class="annotation-layout"><div id="annotations" class="annotation-list"><p class="muted">Cargando anotaciones...</p></div><form id="annotation-form" class="panel-form"><h2>Hacer una cita</h2><select name="tipo"><option value="CITA">Cita</option><option value="ANALISIS">Análisis</option><option value="REFERENCIA">Referencia</option></select><select name="libroId" id="annotation-book" required><option value="">Selecciona un libro</option></select><div class="form-row"><input name="capituloNumero" type="number" min="0" placeholder="Capítulo"><input name="pagina" type="number" min="0" placeholder="Página"></div><textarea name="contenido" maxlength="10000" required placeholder="Escribe aquí la cita o tu nota..."></textarea><button class="primary-button">Guardar anotación →</button></form></div></section>`
  if (!isLoggedIn()) {
    document.querySelector('#annotations').innerHTML = '<div class="empty"><h3>Inicia sesión para ver tus anotaciones</h3><p>Guarda citas y notas de tus libros favoritos.</p><button class="primary-button" id="annotations-login">Iniciar sesión →</button></div>'
    document.querySelector('#annotations-login').addEventListener('click', () => showAuth('login'))
    document.querySelector('#annotation-form').addEventListener('submit', (event) => { event.preventDefault(); showAuth('login') })
  }
  const items = await loadBooks()
  items.forEach((book) => document.querySelector('#annotation-book').insertAdjacentHTML('beforeend', `<option value="${escapeHtml(book.id)}">${escapeHtml(title(book))}</option>`))
  document.querySelector('#annotation-form').addEventListener('submit', submitAnnotation)
  if (!isLoggedIn()) return
  try {
    const annotations = await request('/api/anotaciones')
    document.querySelector('#annotations').innerHTML = annotations.length ? annotations.map((item) => `<article class="annotation-card"><span class="tag">${escapeHtml(item.tipo)}</span><p>${escapeHtml(item.contenido)}</p><small>${item.pagina ? `Página ${escapeHtml(item.pagina)} · ` : ''}${escapeHtml(item.usuario?.username || 'lector')}</small></article>`).join('') : '<p class="muted">Aún no has guardado ninguna anotación.</p>'
  } catch (error) { document.querySelector('#annotations').innerHTML = `<p class="muted">${escapeHtml(error.message)}</p>` }
}
async function submitAnnotation(event) {
  event.preventDefault(); if (!requireLogin()) return
  const data = Object.fromEntries(new FormData(event.target))
  if (!data.capituloNumero) delete data.capituloNumero
  if (!data.pagina) delete data.pagina
  data.libroId = Number(data.libroId)
  try { await request('/api/anotaciones', { method: 'POST', body: JSON.stringify(data) }); showToast('Anotación guardada.'); renderAnnotations() } catch (error) { showToast(error.message) }
}

function showAuth(mode = 'login') {
  modal.hidden = false
  modal.innerHTML = `<div class="modal-backdrop"><div class="auth-modal"><button class="modal-close" id="close-modal" aria-label="Cerrar">×</button><span class="brand-mark">▱</span><p class="eyebrow">Librarium Marginalia</p><h2>${mode === 'login' ? 'Vuelve a tus lecturas' : 'Crea tu cuenta'}</h2><div class="auth-tabs"><button class="${mode === 'login' ? 'active' : ''}" data-auth="login">Iniciar sesión</button><button class="${mode === 'register' ? 'active' : ''}" data-auth="register">Registrarme</button></div><form id="auth-form">${mode === 'register' ? '<input name="username" maxlength="50" required placeholder="Nombre de usuario">' : ''}<input name="email" type="email" required placeholder="Tu email"><input name="password" type="password" minlength="4" required placeholder="Tu contraseña"><button class="primary-button">${mode === 'login' ? 'Entrar en mi biblioteca →' : 'Crear mi cuenta →'}</button></form></div></div>`
  document.querySelector('#close-modal').addEventListener('click', () => { modal.hidden = true })
  document.querySelectorAll('[data-auth]').forEach((button) => button.addEventListener('click', () => showAuth(button.dataset.auth)))
  document.querySelector('#auth-form').addEventListener('submit', async (event) => {
    event.preventDefault()
    const data = Object.fromEntries(new FormData(event.target))
    try {
      if (mode === 'register') { await request('/api/auth/register', { method: 'POST', body: JSON.stringify(data) }); showToast('Cuenta creada. Ahora inicia sesión.'); showAuth('login') }
      else { const result = await request('/api/auth/login', { method: 'POST', body: JSON.stringify(data) }); localStorage.setItem('librarium_token', result.token); modal.hidden = true; updateAuthButton(); showToast('Has entrado en Librarium.') }
    } catch (error) { showToast(error.message) }
  })
}
function updateAuthButton() {
  const button = document.querySelector('#login-button')
  button.innerHTML = isLoggedIn() ? '♙ <span>Salir</span>' : '♙ <span>Entrar</span>'
  button.onclick = () => { if (isLoggedIn()) { localStorage.removeItem('librarium_token'); updateAuthButton(); showToast('Sesión cerrada.') } else showAuth('login') }
}
function showPage(page) {
  currentPage = ['inicio', 'catalogo', 'descubrir', 'favoritos', 'detalle', 'comunidad', 'anotaciones'].includes(page) ? page : 'inicio'
  document.querySelectorAll('[data-page]').forEach((link) => link.classList.toggle('active', link.dataset.page === currentPage))
  if (currentPage === 'inicio') renderHome()
  if (currentPage === 'catalogo') renderCatalog()
  if (currentPage === 'descubrir') renderDiscover()
  if (currentPage === 'favoritos') renderFavorites()
  if (currentPage === 'detalle') renderDetail()
  if (currentPage === 'comunidad') renderCommunity()
  if (currentPage === 'anotaciones') renderAnnotations()
  document.querySelector('#main-nav').classList.remove('open')
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

document.querySelectorAll('[data-page]').forEach((link) => link.addEventListener('click', (event) => { event.preventDefault(); showPage(link.dataset.page) }))
document.querySelector('#menu-button').addEventListener('click', () => document.querySelector('#main-nav').classList.toggle('open'))
window.addEventListener('hashchange', () => showPage(location.hash.slice(1) || 'inicio'))
updateAuthButton()
showPage(location.hash.slice(1) || 'inicio')
