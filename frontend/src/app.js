import config from '../config.js'

const API_URL = config.apiUrl.replace(/\/$/, '')
const iconUrl = new URL('media/librarium-icon.svg', document.baseURI).href
const fallbackCover = iconUrl
const app = document.querySelector('#app')
const modal = document.querySelector('#modal')
let books = []
let currentBook = null
let currentProfile = null
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
  const responseText = response.status === 204 ? '' : await response.text()
  let responseBody = null
  if (responseText.trim()) {
    try { responseBody = JSON.parse(responseText) } catch { responseBody = responseText }
  }
  if (!response.ok) {
    const bodyMessage = responseBody && typeof responseBody === 'object'
      ? responseBody.message || responseBody.error
      : typeof responseBody === 'string' && !/^\s*</.test(responseBody) ? responseBody : null
    const fallbackMessage = /^\s*</.test(responseText)
      ? `La API respondió con el estado ${response.status}, pero devolvió una página HTML. Comprueba que el backend y el frontend estén desplegados en sus versiones compatibles.`
      : `La API respondió con el estado ${response.status} y no devolvió un error JSON válido.`
    const requestError = new Error(bodyMessage || fallbackMessage)
    requestError.status = response.status
    throw requestError
  }
  return responseBody
}

function isLoggedIn() { return Boolean(localStorage.getItem('librarium_token')) }
function tokenPayload(token) {
  const parts = token.split('.')
  if (parts.length !== 3) return null
  const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
  return JSON.parse(atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')))
}
function isAdmin() {
  if (!isLoggedIn()) return false
  const token = localStorage.getItem('librarium_token')
  if (token) {
    try {
      const payload = tokenPayload(token)
      const roles = payload?.roles || []
      if (Array.isArray(roles) && (roles.includes('ROLE_ADMIN') || roles.includes('ADMIN'))) {
        return true
      }
    } catch (e) {
      // Ignorar errores de decodificación
    }
  }
  const role = localStorage.getItem('librarium_role')
  return role === 'ROLE_ADMIN' || role === 'ADMIN'
}
function clearLegacySession() {
  const token = localStorage.getItem('librarium_token')
  if (!token) return false
  try {
    const payload = tokenPayload(token)
    const username = localStorage.getItem('librarium_username')
    if (payload?.sub && username && !payload.sub.includes('@') && payload.sub.toLowerCase() === username.toLowerCase()) {
      return false
    }
  } catch (e) {
    // Las sesiones que no se pueden leer deben volver a autenticarse.
  }
  localStorage.removeItem('librarium_token')
  localStorage.removeItem('librarium_username')
  localStorage.removeItem('librarium_role')
  return true
}
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
function synopsis(book) {
  const text = book?.sinopsis || book?.synopsis || book?.description || ''
  return text.trim() || 'Sin sinopsis disponible.'
}
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
    <p class="book-synopsis">${escapeHtml(synopsis(book))}</p>
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
function bindProfileLinks() {
  document.querySelectorAll('[data-profile]').forEach((element) => element.addEventListener('click', () => {
    currentProfile = element.dataset.profile
    showPage('perfil')
  }))
}

function renderHome() {
  app.innerHTML = `<section class="hero"><div class="hero-copy"><p class="eyebrow">✦ Tu biblioteca, con margen para pensar</p><h1>Historias que<br><em>dejan huella.</em></h1><p class="hero-text">Descubre libros, guarda tus hallazgos y comparte las ideas que aparecen entre líneas.</p>${searchForm()}<button class="text-button" data-go="catalogo">Explorar catálogo →</button></div></section><section class="section"><div class="section-heading"><div><p class="eyebrow">La estantería de hoy</p><h2>Lecturas que recomendamos</h2></div><button class="text-button" data-go="catalogo">Ver todo →</button></div><div id="home-books" class="book-grid"><p class="muted">Cargando la estantería...</p></div></section><section class="feature-strip"><div><p class="eyebrow">Más que leer</p><h2>Deja tu propia huella</h2><p>Publica una reseña, abre un hilo para conversar o guarda una cita que quieras recordar.</p></div><div class="feature-actions"><button class="primary-button" data-go="comunidad">Entrar en la comunidad</button><button class="outline-button" data-go="anotaciones">Guardar una cita</button></div></section>`
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
    target.innerHTML = externalBooks.length ? `<div class="external-grid">${externalBooks.map((book, index) => `<article class="external-card"><img src="${escapeHtml(book.coverUrl || book.portadaUrl || fallbackCover)}" alt="Portada de ${escapeHtml(book.title || book.titulo || 'Sin título')}"><div class="external-info"><h3>${escapeHtml(book.title || book.titulo || 'Sin título')}</h3><p>${escapeHtml((book.authors || book.autores || []).join(', ') || 'Autor desconocido')}</p><small class="external-year">${escapeHtml(book.firstPublishYear || book.anoPublicacion || 'Año desconocido')}</small><button class="outline-button" data-import-index="${index}">Añadir a la biblioteca</button></div></article>`).join('')}</div>` : '<p class="muted">No hemos encontrado resultados externos.</p>'
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
  app.innerHTML = `<section class="section page"><div class="page-intro"><p class="eyebrow">✦ Personas y lecturas</p><h1>Descubre tu próxima conversación</h1><p>Encuentra lectores por su nick y conoce sus opiniones, citas y blogs.</p></div><form class="external-search" id="user-search"><input name="q" placeholder="Buscar usuario por nick..."><button class="outline-button">Buscar lectores →</button></form><div class="section-heading discover-heading"><div><p class="eyebrow">Lectores de Librarium</p><h2>Perfiles para descubrir</h2></div></div><div id="user-results" class="user-grid"><p class="muted">Cargando lectores...</p></div><div class="section-heading discover-heading"><div><p class="eyebrow">Recomendaciones</p><h2>Lecturas para explorar</h2></div></div><div id="discover-books" class="book-grid"><p class="muted">Cargando recomendaciones...</p></div></section>`
  document.querySelector('#user-search').addEventListener('submit', searchUsers)
  loadUsers()
  loadBooks().then((items) => { const target = document.querySelector('#discover-books'); if (target) { target.innerHTML = bookGrid([...items].sort(() => Math.random() - .5).slice(0, 8)); bindBookActions() } })
}
async function loadUsers(query = '') {
  const target = document.querySelector('#user-results')
  if (!target) return
  target.innerHTML = '<p class="muted">Cargando lectores...</p>'
  try {
    const users = await request(`/api/usuarios${query ? `?q=${encodeURIComponent(query)}` : ''}`)
    const publicUsers = users.filter(user => user && typeof user.username === 'string')
    target.innerHTML = publicUsers.length ? publicUsers.map(user => `
      <button class="user-card" data-profile="${escapeHtml(user.username)}">
        <span class="user-avatar">${escapeHtml(user.username.slice(0, 1).toUpperCase())}</span>
        <span>
          <strong>@${escapeHtml(user.username)}</strong>
          <small>${user.descripcion ? escapeHtml(user.descripcion) : 'Ver perfil público y publicaciones →'}</small>
        </span>
      </button>
    `).join('') : '<p class="muted">Todavía no hay lectores disponibles.</p>'
    bindProfileLinks()
  } catch (error) { target.innerHTML = `<p class="muted">${escapeHtml(error.message)}</p>` }
}
async function searchUsers(event) {
  event.preventDefault()
  const query = new FormData(event.target).get('q')
  await loadUsers(query.trim())
}
async function renderProfile() {
  if (!currentProfile) return showPage('descubrir')
  app.innerHTML = `<section class="section page profile-page"><button class="back-button" data-go="descubrir">← Volver a descubrir</button><div id="profile-content"><p class="muted">Cargando perfil...</p></div></section>`
  bindNavigation()
  try {
    const profile = await request(`/api/usuarios/${encodeURIComponent(currentProfile)}`)
    const myUsername = localStorage.getItem('librarium_username')
    const isMine = isLoggedIn() && (profile.usuario?.username?.toLowerCase() === myUsername?.toLowerCase() || currentProfile?.toLowerCase() === myUsername?.toLowerCase())
    await renderProfileView(profile, isMine)
  } catch (error) { document.querySelector('#profile-content').innerHTML = `<div class="empty"><h3>No se pudo cargar el perfil</h3><p>${escapeHtml(error.message)}</p></div>` }
}
async function renderMyProfile() {
  if (!isLoggedIn()) {
    app.innerHTML = `<section class="section page"><div class="page-intro"><p class="eyebrow">Mi perfil</p><h1>Tu espacio en Librarium</h1><p>Consulta tus opiniones, blogs, citas y actualiza tus datos de usuario.</p></div><div class="empty"><h3>Inicia sesión para ver tu perfil</h3><p>Accede a tu cuenta para consultar tu actividad, tus lecturas y editar tus datos de perfil.</p><button class="primary-button" id="myprofile-login">Iniciar sesión →</button></div></section>`
    document.querySelector('#myprofile-login').addEventListener('click', () => showAuth('login'))
    return
  }
  app.innerHTML = `<section class="section page profile-page"><div id="profile-content"><p class="muted">Cargando tu perfil...</p></div></section>`
  try {
    const profile = await request('/api/usuarios/me')
    await renderProfileView(profile, true)
  } catch (error) {
    if (error.status === 401 || error.status === 404) {
      localStorage.removeItem('librarium_token')
      localStorage.removeItem('librarium_username')
      updateAuthButton()
      app.innerHTML = '<section class="section page"><div class="empty"><h3>Tu sesión necesita actualizarse</h3><p>La cuenta asociada a esta sesión ya no está disponible. Inicia sesión de nuevo con tu cuenta actual.</p><button class="primary-button" id="profile-login">Iniciar sesión →</button></div></section>'
      document.querySelector('#profile-login').addEventListener('click', () => showAuth('login'))
      return
    }
    app.innerHTML = `<section class="section page"><div class="empty"><h3>No se pudo cargar tu perfil</h3><p>${escapeHtml(error.message)}</p></div></section>`
  }
}
async function renderProfileView(profile, isMine) {
  const content = document.querySelector('#profile-content')
  if (!content) return
  const name = profile.usuario?.username || currentProfile || 'lector'
  const bio = profile.descripcion || profile.usuario?.descripcion || ''
  const bookMap = new Map((await loadBooks()).map(book => [String(book.id), book]))
  const bookName = id => bookMap.get(String(id)) ? title(bookMap.get(String(id))) : 'Libro del catálogo'
  const reviews = profile.resenas || []
  const threads = profile.hilos || []
  const annotations = profile.anotaciones || []
  const dateStr = profile.fechaRegistro ? new Date(profile.fechaRegistro).toLocaleDateString('es-ES') : 'hace poco'

  const isAdminUser = (isMine && isAdmin()) || name.toLowerCase() === 'admin'
  const adminBadgeHtml = isAdminUser ? '<span class="admin-badge">♛ Administrador</span>' : ''

  content.innerHTML = `
    <div class="profile-header">
      <span class="profile-avatar">${escapeHtml(name.slice(0, 1).toUpperCase())}</span>
      <div style="flex:1;">
        <p class="eyebrow">${isMine ? 'Mi perfil' : 'Perfil público'} ${adminBadgeHtml}</p>
        <h1>@${escapeHtml(name)}</h1>
        ${bio ? `<p class="profile-bio">${escapeHtml(bio)}</p>` : (isMine ? '<p class="profile-bio muted">Añade una descripción sobre ti para que otros lectores te conozcan.</p>' : '')}
        <p class="profile-joined">Lector de Librarium desde ${escapeHtml(dateStr)}</p>
        ${profile.usuario?.email ? `<p class="profile-joined">Correo de contacto: <a href="mailto:${escapeHtml(profile.usuario.email)}">${escapeHtml(profile.usuario.email)}</a></p>` : ''}
      </div>
    </div>
    <div class="profile-stats">
      <span><b>${reviews.length}</b> opiniones</span>
      <span><b>${annotations.length}</b> citas y notas</span>
      <span><b>${threads.length}</b> blogs</span>
    </div>
    ${isMine ? `
      <div class="profile-edit-box">
        <div class="section-heading">
          <div>
            <p class="eyebrow">Ajustes de mi cuenta</p>
            <h2>Editar información de perfil</h2>
          </div>
        </div>
        <form id="edit-profile-form" class="panel-form" style="max-width:680px;margin-bottom:30px;">
          <div>
            <label for="edit-username" style="display:block;margin-bottom:6px;font-size:11px;font-weight:700;color:var(--soft);">Nombre de usuario</label>
            <input id="edit-username" name="username" type="text" maxlength="50" value="${escapeHtml(name)}" placeholder="Tu nombre de usuario (opcional)">
          </div>
          <div>
            <label for="edit-descripcion" style="display:block;margin-bottom:6px;font-size:11px;font-weight:700;color:var(--soft);">Descripción / Biografía</label>
            <textarea id="edit-descripcion" name="descripcion" maxlength="1000" rows="3" placeholder="Escribe unas líneas sobre ti, tus autores favoritos o tus temas de interés...">${escapeHtml(bio)}</textarea>
          </div>
          <div>
            <button type="submit" class="primary-button">Guardar cambios →</button>
          </div>
        </form>
      </div>
    ` : ''}
    <div class="profile-columns">
      <div>
        <section class="profile-section">
          <p class="eyebrow">Opiniones</p>
          <h2>${isMine ? 'Mis opiniones sobre lecturas' : 'Lo que piensa sobre sus lecturas'}</h2>
          <div class="profile-feed">
            ${reviews.length ? reviews.map(item => `
              <article class="review">
                <div class="review-stars">${'★'.repeat(item.puntuacion || 0)}${'☆'.repeat(5 - (item.puntuacion || 0))}</div>
                <p>${escapeHtml(item.comentario || 'Sin comentario')}</p>
                <small>${escapeHtml(bookName(item.libroId))}</small>
              </article>
            `).join('') : '<p class="muted">Todavía no hay opiniones publicadas.</p>'}
          </div>
        </section>
        <section class="profile-section">
          <p class="eyebrow">Blog</p>
          <h2>${isMine ? 'Mis conversaciones en la comunidad' : 'Conversaciones iniciadas'}</h2>
          <div class="profile-feed">
            ${threads.length ? threads.map(item => `
              <article class="thread-card">
                <div>
                  <h2>${escapeHtml(item.tituloHilo)}</h2>
                  <p>${escapeHtml(item.contenidoInicial)}</p>
                  <small>${escapeHtml(bookName(item.libroId))}</small>
                </div>
              </article>
            `).join('') : '<p class="muted">Todavía no hay blogs iniciados.</p>'}
          </div>
        </section>
      </div>
      <aside class="profile-aside">
        <section class="profile-section">
          <p class="eyebrow">Citas</p>
          <h2>${isMine ? 'Mis citas y anotaciones' : 'Marginalia pública'}</h2>
          <div class="profile-feed">
            ${annotations.length ? annotations.map(item => `
              <article class="annotation-card">
                <span class="tag">${escapeHtml(item.tipo)}</span>
                <p>“${escapeHtml(item.contenido)}”</p>
                <small>${escapeHtml(bookName(item.libroId))}</small>
              </article>
            `).join('') : '<p class="muted">Todavía no hay citas compartidas.</p>'}
          </div>
        </section>
      </aside>
    </div>
  `

  if (isMine) {
    const editForm = document.querySelector('#edit-profile-form')
    if (editForm) {
      editForm.addEventListener('submit', async (event) => {
        event.preventDefault()
        const formData = new FormData(event.target)
        const newUsername = (formData.get('username') || '').trim()
        const newDescripcion = (formData.get('descripcion') || '').trim()
        try {
          const body = { descripcion: newDescripcion }
          if (newUsername) {
            body.username = newUsername
          }
          const updated = await request('/api/usuarios/me', {
            method: 'PUT',
            body: JSON.stringify(body)
          })
          if (updated.usuario?.username) {
            localStorage.setItem('librarium_username', updated.usuario.username)
            currentProfile = updated.usuario.username
          }
          showToast('Perfil actualizado con éxito.')
          await renderProfileView(updated, true)
        } catch (err) {
          showToast(err.message)
        }
      })
    }
  }
}
function renderFavorites() {
  app.innerHTML = `<section class="section page"><div class="page-intro"><p class="eyebrow">♡ Tu biblioteca personal</p><h1>Mis lecturas</h1><p>Los libros que has querido guardar para volver a ellos.</p></div>${bookGrid(getFavorites())}</section>`
  bindBookActions()
}
async function renderDetail() {
  if (!currentBook) return showPage('catalogo')
  const id = bookId(currentBook)
  app.innerHTML = `<section class="section page detail-page"><button class="back-button" data-go="catalogo">← Volver al catálogo</button><div class="detail-layout"><img class="detail-cover" src="${escapeHtml(cover(currentBook))}" alt="Portada de ${escapeHtml(title(currentBook))}"><div class="detail-copy"><p class="eyebrow">Ficha de lectura</p><h1>${escapeHtml(title(currentBook))}</h1><p class="detail-author">${escapeHtml(authors(currentBook))}</p><p class="detail-description" id="detail-synopsis">${escapeHtml(synopsis(currentBook))}</p><p class="detail-meta"><b>${escapeHtml(year(currentBook))}</b> año</p><button class="outline-button" id="save-button">${isFavorite(currentBook) ? '♡ Guardado en mis lecturas' : '♡ Guardar en mis lecturas'}</button><button class="outline-button" data-go="anotaciones">✎ Añadir anotación</button></div></div><div class="reviews"><p class="eyebrow">La conversación</p><h2>Reseñas de lectores</h2><div id="reviews">Cargando reseñas...</div><form id="review-form" class="review-form"><h3>Deja tu impresión</h3><select name="puntuacion"><option value="5">★★★★★ · Imprescindible</option><option value="4">★★★★☆ · Muy recomendable</option><option value="3">★★★☆☆ · Interesante</option><option value="2">★★☆☆☆ · Irregular</option><option value="1">★☆☆☆☆ · No conecté</option></select><textarea name="comentario" maxlength="5000" placeholder="¿Qué te ha hecho sentir este libro?"></textarea><button class="primary-button">Publicar reseña →</button></form></div></section>`
  bindNavigation()
  document.querySelector('#save-button').addEventListener('click', () => { toggleFavorite(currentBook); renderDetail() })
  document.querySelector('#review-form').addEventListener('submit', submitReview)

  if (id && !isNaN(Number(id))) {
    try {
      const detailed = await request(`/api/libros/${id}`)
      if (detailed) {
        currentBook = { ...currentBook, ...detailed }
        const synopsisEl = document.querySelector('#detail-synopsis')
        if (synopsisEl && detailed.sinopsis) {
          synopsisEl.textContent = detailed.sinopsis
        }
      }
    } catch (err) {
      console.warn('No se pudo cargar el detalle del libro', err)
    }
  }

  try {
    const reviews = await request(`/api/resenas?libroId=${id}`)
    document.querySelector('#reviews').innerHTML = reviews.length ? reviews.map(reviewHtml).join('') : '<p class="muted">Todavía no hay reseñas. Sé la primera persona en dejar una.</p>'
    bindProfileLinks()
    document.querySelectorAll('[data-delete-review]').forEach((btn) => btn.addEventListener('click', async (e) => {
      e.stopPropagation()
      if (!isAdmin()) return
      if (!confirm('¿Deseas eliminar esta reseña como Administrador?')) return
      try {
        await request(`/api/resenas/${btn.dataset.deleteReview}`, { method: 'DELETE' })
        showToast('Reseña eliminada por el administrador.')
        renderDetail()
      } catch (err) {
        showToast(err.message)
      }
    }))
  } catch (error) { document.querySelector('#reviews').innerHTML = `<p class="muted">${escapeHtml(error.message)}</p>` }
}
function reviewHtml(item) {
  const adminMod = isAdmin()
    ? `<button class="admin-mod-btn" data-delete-review="${item.id}" title="Eliminar reseña como Administrador">🗑 Moderar</button>`
    : ''
  return `<article class="review"><div style="display:flex;justify-content:space-between;align-items:center;"><div class="review-stars">${'★'.repeat(item.puntuacion || 0)}${'☆'.repeat(5 - (item.puntuacion || 0))}</div>${adminMod}</div><p>${escapeHtml(item.comentario || 'Sin comentario')}</p><small>Por <button class="inline-profile" data-profile="${escapeHtml(item.usuario?.username || '')}">@${escapeHtml(item.usuario?.username || 'lector')}</button></small></article>`
}
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
    bindProfileLinks()
    document.querySelectorAll('[data-thread]').forEach((element) => element.addEventListener('click', () => renderThread(Number(element.dataset.thread), element.dataset.title)))
    document.querySelectorAll('[data-delete-thread]').forEach((btn) => btn.addEventListener('click', async (e) => {
      e.stopPropagation()
      if (!isAdmin()) return
      if (!confirm('¿Deseas eliminar este hilo de la comunidad como Administrador?')) return
      try {
        await request(`/api/foros/${btn.dataset.deleteThread}`, { method: 'DELETE' })
        showToast('Hilo eliminado correctamente.')
        renderCommunity()
      } catch (err) {
        showToast(err.message)
      }
    }))
  } catch (error) { document.querySelector('#threads').innerHTML = `<p class="muted">${escapeHtml(error.message)}</p>` }
}
function threadHtml(item) {
  const adminMod = isAdmin()
    ? `<button class="admin-mod-btn" data-delete-thread="${item.id}" title="Eliminar hilo como Administrador">🗑 Moderar</button>`
    : ''
  return `<article class="thread-card"><div><div style="display:flex;justify-content:space-between;align-items:center;"><p class="eyebrow">Hilo lector</p>${adminMod}</div><h2>${escapeHtml(item.tituloHilo)}</h2><p>${escapeHtml(item.contenidoInicial)}</p><small>Por <button class="inline-profile" data-profile="${escapeHtml(item.usuario?.username || '')}">@${escapeHtml(item.usuario?.username || 'lector')}</button> · ${escapeHtml(item.fechaCreacion || '')}</small></div><button class="outline-button" data-thread="${item.id}" data-title="${escapeHtml(item.tituloHilo)}">Ver respuestas →</button></article>`
}
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
    document.querySelector('#replies').innerHTML = replies.length ? replies.map((item) => {
      const adminMod = isAdmin()
        ? `<button class="admin-mod-btn" data-delete-reply="${item.id}" title="Eliminar respuesta como Administrador">🗑 Moderar</button>`
        : ''
      return `<article class="reply"><div style="display:flex;justify-content:space-between;align-items:flex-start;"><p style="margin:0;">${escapeHtml(item.contenido)}</p>${adminMod}</div><small><button class="inline-profile" data-profile="${escapeHtml(item.usuario?.username || '')}">@${escapeHtml(item.usuario?.username || 'lector')}</button> · ${escapeHtml(item.fechaCreacion || '')}</small></article>`
    }).join('') : '<p class="muted">Todavía no hay respuestas.</p>'
    bindProfileLinks()
    document.querySelectorAll('[data-delete-reply]').forEach((btn) => btn.addEventListener('click', async (e) => {
      e.stopPropagation()
      if (!isAdmin()) return
      if (!confirm('¿Deseas eliminar esta respuesta como Administrador?')) return
      try {
        await request(`/api/foros/respuestas/${btn.dataset.deleteReply}`, { method: 'DELETE' })
        showToast('Respuesta eliminada.')
        renderThread(id, threadTitle)
      } catch (err) {
        showToast(err.message)
      }
    }))
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
    document.querySelector('#annotations').innerHTML = annotations.length ? annotations.map((item) => {
      const adminMod = isAdmin()
        ? `<button class="admin-mod-btn" data-delete-annotation="${item.id}" title="Eliminar anotación como Administrador">🗑 Moderar</button>`
        : ''
      return `<article class="annotation-card"><div style="display:flex;justify-content:space-between;align-items:center;"><span class="tag">${escapeHtml(item.tipo)}</span>${adminMod}</div><p>${escapeHtml(item.contenido)}</p><small>${item.pagina ? `Página ${escapeHtml(item.pagina)} · ` : ''}${escapeHtml(item.usuario?.username || 'lector')}</small></article>`
    }).join('') : '<p class="muted">Aún no has guardado ninguna anotación.</p>'
    document.querySelectorAll('[data-delete-annotation]').forEach((btn) => btn.addEventListener('click', async (e) => {
      e.stopPropagation()
      if (!isAdmin()) return
      if (!confirm('¿Deseas eliminar esta anotación como Administrador?')) return
      try {
        await request(`/api/anotaciones/${btn.dataset.deleteAnnotation}`, { method: 'DELETE' })
        showToast('Anotación eliminada.')
        renderAnnotations()
      } catch (err) {
        showToast(err.message)
      }
    }))
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

let currentAdminTab = 'moderacion'
let currentAdminModFilter = 'todas'

function showAuth(mode = 'login') {
  modal.hidden = false
  const identityFields = mode === 'register'
    ? '<input name="username" maxlength="50" autocomplete="username" required placeholder="Nick único"><input name="email" type="email" autocomplete="email" required placeholder="Correo de contacto">'
    : '<input name="username" maxlength="50" autocomplete="username" required placeholder="Tu nick">'
  modal.innerHTML = `<div class="modal-backdrop"><div class="auth-modal"><button class="modal-close" id="close-modal" aria-label="Cerrar">×</button><img class="brand-mark" src="${iconUrl}" alt=""><p class="eyebrow">Librarium Marginalia</p><h2>${mode === 'login' ? 'Vuelve a tus lecturas' : 'Crea tu cuenta'}</h2><div class="auth-tabs"><button class="${mode === 'login' ? 'active' : ''}" data-auth="login">Iniciar sesión</button><button class="${mode === 'register' ? 'active' : ''}" data-auth="register">Registrarme</button></div><form id="auth-form">${identityFields}<input name="password" type="password" minlength="4" autocomplete="${mode === 'login' ? 'current-password' : 'new-password'}" required placeholder="Tu contraseña"><button class="primary-button">${mode === 'login' ? 'Entrar en mi biblioteca →' : 'Crear mi cuenta →'}</button></form></div></div>`
  document.querySelector('#close-modal').addEventListener('click', () => { modal.hidden = true })
  document.querySelectorAll('[data-auth]').forEach((button) => button.addEventListener('click', () => showAuth(button.dataset.auth)))
  document.querySelector('#auth-form').addEventListener('submit', async (event) => {
    event.preventDefault()
    const data = Object.fromEntries(new FormData(event.target))
    try {
      if (mode === 'register') {
        await request('/api/auth/register', { method: 'POST', body: JSON.stringify(data) })
        showToast('Cuenta creada. Ahora inicia sesión.')
        showAuth('login')
      } else {
        const result = await request('/api/auth/login', { method: 'POST', body: JSON.stringify(data) })
        if (!result || typeof result !== 'object' || !result.token || !result.username) {
          throw new Error('La API no devolvió una respuesta de inicio de sesión válida. Comprueba que el backend y el frontend estén desplegados en versiones compatibles.')
        }
        localStorage.setItem('librarium_token', result.token)
        localStorage.setItem('librarium_username', result.username)
        if (result.rol) {
          localStorage.setItem('librarium_role', result.rol)
        } else {
          try {
            const payload = tokenPayload(result.token)
            if (payload.roles && (payload.roles.includes('ROLE_ADMIN') || payload.roles.includes('ADMIN'))) {
              localStorage.setItem('librarium_role', 'ROLE_ADMIN')
            } else {
              localStorage.setItem('librarium_role', 'ROLE_USER')
            }
          } catch (e) {}
        }
        modal.hidden = true
        updateAuthButton()
        updateNav()
        if (isAdmin()) {
          showToast(`¡Bienvenido administrador @${result.username}!`)
          showPage('admin')
        } else {
          showToast(`Has entrado en Librarium como @${result.username}.`)
        }
      }
    } catch (error) { showToast(error.message) }
  })
}

function updateNav() {
  const navAdmin = document.querySelector('#nav-admin')
  if (navAdmin) {
    navAdmin.style.display = isAdmin() ? 'flex' : 'none'
  }
}

function updateAuthButton() {
  const button = document.querySelector('#login-button')
  const username = localStorage.getItem('librarium_username')
  if (isLoggedIn()) {
    button.innerHTML = isAdmin()
      ? `♛ <span>${escapeHtml(username || 'Admin')} (Salir)</span>`
      : `♙ <span>${escapeHtml(username || 'Salir')}</span>`
    button.onclick = () => {
      localStorage.removeItem('librarium_token')
      localStorage.removeItem('librarium_username')
      localStorage.removeItem('librarium_role')
      updateAuthButton()
      updateNav()
      showToast('Sesión cerrada.')
      if (currentPage === 'admin' || currentPage === 'mi-perfil') {
        showPage('inicio')
      }
    }
  } else {
    button.innerHTML = '♙ <span>Entrar</span>'
    button.onclick = () => showAuth('login')
  }
  updateNav()
}

function showPage(page) {
  if (page === 'admin' && !isAdmin()) {
    page = 'inicio'
  }
  currentPage = ['inicio', 'catalogo', 'descubrir', 'favoritos', 'comunidad', 'anotaciones', 'perfil', 'mi-perfil', 'detalle', 'admin'].includes(page) ? page : 'inicio'
  updateNav()
  document.querySelectorAll('[data-page]').forEach((link) => link.classList.toggle('active', link.dataset.page === currentPage))
  if (currentPage === 'inicio') renderHome()
  if (currentPage === 'catalogo') renderCatalog()
  if (currentPage === 'descubrir') renderDiscover()
  if (currentPage === 'favoritos') renderFavorites()
  if (currentPage === 'detalle') renderDetail()
  if (currentPage === 'comunidad') renderCommunity()
  if (currentPage === 'anotaciones') renderAnnotations()
  if (currentPage === 'perfil') renderProfile()
  if (currentPage === 'mi-perfil') renderMyProfile()
  if (currentPage === 'admin') renderAdmin()
  document.querySelector('#main-nav').classList.remove('open')
  document.querySelector('#menu-button').setAttribute('aria-expanded', 'false')
  document.querySelector('#menu-button').setAttribute('aria-label', 'Abrir menú')
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

async function renderAdmin() {
  if (!isAdmin()) {
    showPage('inicio')
    return
  }
  app.innerHTML = `
    <section class="section page admin-page">
      <div class="page-intro">
        <p class="eyebrow">♛ Panel de Administración</p>
        <h1>Consola de Control</h1>
        <p>Gestión centralizada de la plataforma: moderación de contenidos, supervisión de lectores, catálogo de obras y métricas del sistema.</p>
      </div>

      <div class="admin-stats-grid">
        <article class="admin-stat-card"><p>Libros en Catálogo</p><h3 id="stat-books">...</h3><small>Títulos disponibles</small></article>
        <article class="admin-stat-card"><p>Lectores Registrados</p><h3 id="stat-users">...</h3><small>Cuentas activas</small></article>
        <article class="admin-stat-card"><p>Blogs en Comunidad</p><h3 id="stat-threads">...</h3><small>Hilos de debate</small></article>
        <article class="admin-stat-card"><p>Reseñas Publicadas</p><h3 id="stat-reviews">...</h3><small>Opiniones de lectores</small></article>
        <article class="admin-stat-card"><p>Citas y Notas</p><h3 id="stat-annotations">...</h3><small>Marginalia guardada</small></article>
      </div>

      <div class="admin-tabs">
        <button class="admin-tab-btn ${currentAdminTab === 'moderacion' ? 'active' : ''}" data-admin-tab="moderacion">🛡 Moderación de Contenidos</button>
        <button class="admin-tab-btn ${currentAdminTab === 'usuarios' ? 'active' : ''}" data-admin-tab="usuarios">👥 Directorio de Lectores</button>
        <button class="admin-tab-btn ${currentAdminTab === 'catalogo' ? 'active' : ''}" data-admin-tab="catalogo">📚 Gestión del Catálogo</button>
        <button class="admin-tab-btn ${currentAdminTab === 'sistema' ? 'active' : ''}" data-admin-tab="sistema">⚙ Estado y Sistema</button>
      </div>

      <div id="admin-tab-content">
        <p class="muted">Cargando sección de administración...</p>
      </div>
    </section>
  `
  bindNavigation()
  document.querySelectorAll('[data-admin-tab]').forEach((btn) => {
    btn.addEventListener('click', () => {
      currentAdminTab = btn.dataset.adminTab
      document.querySelectorAll('[data-admin-tab]').forEach((b) => b.classList.toggle('active', b.dataset.adminTab === currentAdminTab))
      renderAdminTabContent()
    })
  })
  loadAdminMetrics()
  renderAdminTabContent()
}

async function loadAdminMetrics() {
  try {
    const [booksList, usersList, threadsList, reviewsList, annotationsList] = await Promise.all([
      request('/api/libros').catch(() => []),
      request('/api/usuarios').catch(() => []),
      request('/api/foros').catch(() => []),
      request('/api/resenas').catch(() => []),
      request('/api/anotaciones').catch(() => [])
    ])
    const b = document.querySelector('#stat-books')
    const u = document.querySelector('#stat-users')
    const t = document.querySelector('#stat-threads')
    const r = document.querySelector('#stat-reviews')
    const a = document.querySelector('#stat-annotations')
    if (b) b.textContent = booksList.length
    if (u) u.textContent = usersList.length
    if (t) t.textContent = threadsList.length
    if (r) r.textContent = reviewsList.length
    if (a) a.textContent = annotationsList.length
  } catch (err) {
    console.warn('Error al cargar métricas de administración', err)
  }
}

async function renderAdminTabContent() {
  const container = document.querySelector('#admin-tab-content')
  if (!container) return
  if (!isAdmin()) return

  if (currentAdminTab === 'moderacion') {
    container.innerHTML = '<div class="admin-panel-box"><p class="muted">Cargando contenidos para moderación...</p></div>'
    try {
      const [reviews, threads, annotations, bookList] = await Promise.all([
        request('/api/resenas').catch(() => []),
        request('/api/foros').catch(() => []),
        request('/api/anotaciones').catch(() => []),
        loadBooks()
      ])
      const bookMap = new Map(bookList.map(item => [String(item.id), title(item)]))
      const getBookTitle = id => bookMap.get(String(id)) || 'Libro del catálogo'

      const items = []
      if (currentAdminModFilter === 'todas' || currentAdminModFilter === 'resenas') {
        reviews.forEach(r => items.push({
          type: 'resena',
          id: r.id,
          badge: '★ RESEÑA',
          author: r.usuario?.username || 'desconocido',
          bookTitle: getBookTitle(r.libroId),
          date: r.fechaRegistro ? new Date(r.fechaRegistro).toLocaleDateString('es-ES') : '—',
          text: (r.puntuacion ? '★'.repeat(r.puntuacion) + ' ' : '') + (r.comentario || 'Sin comentario')
        }))
      }
      if (currentAdminModFilter === 'todas' || currentAdminModFilter === 'hilos') {
        threads.forEach(h => items.push({
          type: 'hilo',
          id: h.id,
          badge: '💬 BLOG / HILO',
          author: h.usuario?.username || 'desconocido',
          bookTitle: getBookTitle(h.libroId),
          date: h.fechaCreacion ? new Date(h.fechaCreacion).toLocaleDateString('es-ES') : '—',
          text: `"${h.tituloHilo}": ${h.contenidoInicial}`
        }))
      }
      if (currentAdminModFilter === 'todas' || currentAdminModFilter === 'anotaciones') {
        annotations.forEach(a => items.push({
          type: 'anotacion',
          id: a.id,
          badge: `✎ ${a.tipo || 'ANOTACIÓN'}`,
          author: a.usuario?.username || 'desconocido',
          bookTitle: getBookTitle(a.libroId),
          date: a.fechaCreacion ? new Date(a.fechaCreacion).toLocaleDateString('es-ES') : '—',
          text: `${a.pagina ? `(Pág. ${a.pagina}) ` : ''}“${a.contenido}”`
        }))
      }

      container.innerHTML = `
        <div class="admin-panel-box">
          <div class="admin-filter-bar">
            <div>
              <p class="eyebrow">Panel de Moderación</p>
              <h2 style="margin:0;font:500 24px 'Playfair Display',serif;">Publicaciones y Contenido de Lectores</h2>
            </div>
            <div class="admin-filter-pills">
              <button class="admin-filter-pill ${currentAdminModFilter === 'todas' ? 'active' : ''}" data-mod-filter="todas">Todos (${reviews.length + threads.length + annotations.length})</button>
              <button class="admin-filter-pill ${currentAdminModFilter === 'resenas' ? 'active' : ''}" data-mod-filter="resenas">Reseñas (${reviews.length})</button>
              <button class="admin-filter-pill ${currentAdminModFilter === 'hilos' ? 'active' : ''}" data-mod-filter="hilos">Blogs (${threads.length})</button>
              <button class="admin-filter-pill ${currentAdminModFilter === 'anotaciones' ? 'active' : ''}" data-mod-filter="anotaciones">Citas (${annotations.length})</button>
            </div>
          </div>
          ${items.length ? items.map(item => `
            <article class="admin-item-card">
              <div class="admin-item-content">
                <div style="display:flex;align-items:center;gap:8px;margin-bottom:6px;">
                  <span class="tag" style="color:var(--red);font-size:10px;font-weight:700;">${escapeHtml(item.badge)}</span>
                  <span style="color:var(--muted);font-size:11px;">· ${escapeHtml(item.date)}</span>
                </div>
                <p style="margin:0 0 8px;color:var(--ink);font-size:14px;line-height:1.5;">${escapeHtml(item.text)}</p>
                <div class="admin-item-meta">
                  <span>Autor: <button class="inline-profile" data-profile="${escapeHtml(item.author)}">@${escapeHtml(item.author)}</button></span>
                  <span>Libro: <b>${escapeHtml(item.bookTitle)}</b></span>
                </div>
              </div>
              <button class="admin-mod-btn" data-admin-del-type="${item.type}" data-admin-del-id="${item.id}">🗑 Eliminar</button>
            </article>
          `).join('') : '<div class="empty"><h3>No hay contenidos en esta categoría</h3><p>La plataforma está libre de elementos pendientes de moderación.</p></div>'}
        </div>
      `
      bindProfileLinks()
      document.querySelectorAll('[data-mod-filter]').forEach(pill => {
        pill.addEventListener('click', () => {
          currentAdminModFilter = pill.dataset.modFilter
          renderAdminTabContent()
        })
      })
      document.querySelectorAll('[data-admin-del-type]').forEach(btn => {
        btn.addEventListener('click', async () => {
          const type = btn.dataset.adminDelType
          const id = btn.dataset.adminDelId
          if (!confirm(`¿Confirmas la eliminación permanente de este elemento (${type}) como Administrador?`)) return
          try {
            if (type === 'resena') await request(`/api/resenas/${id}`, { method: 'DELETE' })
            if (type === 'hilo') await request(`/api/foros/${id}`, { method: 'DELETE' })
            if (type === 'anotacion') await request(`/api/anotaciones/${id}`, { method: 'DELETE' })
            showToast('Elemento eliminado del sistema.')
            loadAdminMetrics()
            renderAdminTabContent()
          } catch (err) {
            showToast(err.message)
          }
        })
      })
    } catch (err) {
      container.innerHTML = `<div class="empty"><h3>Error al cargar moderación</h3><p>${escapeHtml(err.message)}</p></div>`
    }
  } else if (currentAdminTab === 'usuarios') {
    container.innerHTML = '<div class="admin-panel-box"><p class="muted">Cargando directorio de lectores...</p></div>'
    try {
      const users = await request('/api/usuarios')
      container.innerHTML = `
        <div class="admin-panel-box">
          <div class="section-heading" style="border:none;margin-bottom:14px;padding-bottom:0;">
            <div>
              <p class="eyebrow">Gestión de Usuarios</p>
              <h2 style="margin:0;font:500 24px 'Playfair Display',serif;">Directorio de Lectores Registrados (${users.length})</h2>
            </div>
          </div>
          <div class="admin-table-wrap">
            <table class="admin-table">
              <thead>
                <tr>
                  <th>Lector</th>
                  <th>Correo de contacto</th>
                  <th>ID</th>
                  <th>Biografía / Descripción</th>
                  <th>Acción</th>
                </tr>
              </thead>
              <tbody>
                ${users.map(u => `
                  <tr>
                    <td>
                      <div style="display:flex;align-items:center;gap:10px;">
                        <span class="user-avatar" style="width:30px;height:30px;font-size:14px;">${escapeHtml((u.username || 'U').slice(0, 1).toUpperCase())}</span>
                        <strong>@${escapeHtml(u.username)}</strong>
                      </div>
                    </td>
                    <td>${escapeHtml(u.email || '—')}</td>
                    <td>#${escapeHtml(u.id || '—')}</td>
                    <td style="max-width:320px;color:var(--soft);overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${escapeHtml(u.descripcion || 'Sin descripción')}</td>
                    <td>
                      <button class="outline-button" style="padding:4px 10px;font-size:11px;" data-profile="${escapeHtml(u.username)}">Ver Perfil →</button>
                    </td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
        </div>
      `
      bindProfileLinks()
    } catch (err) {
      container.innerHTML = `<div class="empty"><h3>Error al cargar usuarios</h3><p>${escapeHtml(err.message)}</p></div>`
    }
  } else if (currentAdminTab === 'catalogo') {
    container.innerHTML = '<div class="admin-panel-box"><p class="muted">Cargando catálogo de obras...</p></div>'
    try {
      const bookList = await loadBooks()
      container.innerHTML = `
        <div class="admin-panel-box">
          <div class="section-heading" style="border:none;margin-bottom:14px;padding-bottom:0;">
            <div>
              <p class="eyebrow">Gestión del Catálogo</p>
              <h2 style="margin:0;font:500 24px 'Playfair Display',serif;">Obras en la Biblioteca Local (${bookList.length})</h2>
            </div>
          </div>
          <form class="external-search" id="admin-ext-search" style="margin-bottom:20px;">
            <input name="q" required placeholder="Importar obra externa: busca título o autor en Open Library...">
            <button class="primary-button">Buscar e Importar →</button>
          </form>
          <div id="admin-ext-results"></div>
          <div class="admin-table-wrap" style="margin-top:20px;">
            <table class="admin-table">
              <thead>
                <tr>
                  <th>Portada</th>
                  <th>Título</th>
                  <th>Autores</th>
                  <th>Año</th>
                  <th>ISBN / Key</th>
                  <th>Ficha</th>
                </tr>
              </thead>
              <tbody>
                ${bookList.map(b => `
                  <tr>
                    <td><img src="${escapeHtml(cover(b))}" alt="" style="width:36px;height:50px;object-fit:cover;border-radius:2px;background:#17100e;"></td>
                    <td><strong>${escapeHtml(title(b))}</strong></td>
                    <td>${escapeHtml(authors(b))}</td>
                    <td>${escapeHtml(year(b))}</td>
                    <td><small style="color:var(--muted);">${escapeHtml(b.isbn || b.openLibraryKey || '—')}</small></td>
                    <td><button class="outline-button" style="padding:4px 10px;font-size:11px;" data-book="${escapeHtml(bookId(b))}">Ver Ficha →</button></td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
        </div>
      `
      bindBookActions()
      document.querySelector('#admin-ext-search').addEventListener('submit', async (e) => {
        e.preventDefault()
        const query = new FormData(e.target).get('q')
        const target = document.querySelector('#admin-ext-results')
        target.innerHTML = '<p class="muted">Consultando Open Library...</p>'
        try {
          const results = await request(`/api/libros/buscar-externo?q=${encodeURIComponent(query)}`)
          const topResults = results.slice(0, 4)
          target.innerHTML = topResults.length ? `<div class="external-grid">${topResults.map((b, idx) => `
            <article class="external-card">
              <img src="${escapeHtml(b.coverUrl || b.portadaUrl || fallbackCover)}" alt="">
              <div class="external-info">
                <h3>${escapeHtml(b.title || b.titulo || 'Sin título')}</h3>
                <p>${escapeHtml((b.authors || b.autores || []).join(', ') || 'Autor desconocido')}</p>
                <button class="primary-button" style="padding:6px 12px;font-size:11px;" data-admin-import-idx="${idx}">+ Importar a Biblioteca</button>
              </div>
            </article>
          `).join('')}</div>` : '<p class="muted">Sin resultados en Open Library.</p>'

          document.querySelectorAll('[data-admin-import-idx]').forEach(btn => {
            btn.addEventListener('click', async () => {
              const bookToImport = topResults[Number(btn.dataset.adminImportIdx)]
              try {
                await request('/api/libros/importar', { method: 'POST', body: JSON.stringify(bookToImport) })
                showToast('Libro importado con éxito.')
                loadAdminMetrics()
                renderAdminTabContent()
              } catch (err) {
                showToast(err.message)
              }
            })
          })
        } catch (err) {
          target.innerHTML = `<p class="muted">${escapeHtml(err.message)}</p>`
        }
      })
    } catch (err) {
      container.innerHTML = `<div class="empty"><h3>Error al cargar catálogo</h3><p>${escapeHtml(err.message)}</p></div>`
    }
  } else if (currentAdminTab === 'sistema') {
    container.innerHTML = `
      <div class="admin-panel-box">
        <div class="section-heading" style="border:none;margin-bottom:20px;padding-bottom:0;">
          <div>
            <p class="eyebrow">Diagnóstico y Estado</p>
            <h2 style="margin:0;font:500 24px 'Playfair Display',serif;">Información del Entorno Librarium</h2>
          </div>
        </div>
        <div class="admin-stats-grid" style="margin-top:0;">
          <article class="admin-stat-card"><p>Estado de la API</p><h3 style="color:#66bb6a;font-size:24px;">● OPERATIVO</h3><small>Conexión segura establecida</small></article>
          <article class="admin-stat-card"><p>Rol de Sesión</p><h3 style="font-size:24px;">ROLE_ADMIN</h3><small>Permisos totales activos</small></article>
          <article class="admin-stat-card"><p>Servidor Backend</p><h3 style="font-size:24px;">Spring Boot 4.1.2</h3><small>Java 25 LTS Runtime</small></article>
          <article class="admin-stat-card"><p>Base de Datos</p><h3 style="font-size:24px;">MySQL Relacional</h3><small>Contenedor Docker :3308</small></article>
        </div>
        <div style="margin-top:20px;padding:18px;border:1px solid var(--line);background:#1a130f;border-radius:4px;">
          <p class="eyebrow">Sesión de Administrador</p>
          <p style="margin:6px 0;color:var(--soft);">Usuario autenticado: <strong>@${escapeHtml(localStorage.getItem('librarium_username') || 'admin')}</strong></p>
          <p style="margin:6px 0;color:var(--soft);">Origen de la API: <code>${escapeHtml(API_URL || 'Mismo origen (Producción)')}</code></p>
          <p style="margin:6px 0;color:var(--soft);">Estrategia de Seguridad: <code>JWT Stateless Bearer Authentication con BCrypt</code></p>
        </div>
      </div>
    `
  }
}

document.querySelectorAll('[data-page]').forEach((link) => link.addEventListener('click', (event) => { event.preventDefault(); showPage(link.dataset.page) }))
document.querySelector('#menu-button').addEventListener('click', () => {
  const button = document.querySelector('#menu-button')
  const nav = document.querySelector('#main-nav')
  const open = nav.classList.toggle('open')
  button.setAttribute('aria-expanded', String(open))
  button.setAttribute('aria-label', open ? 'Cerrar menú' : 'Abrir menú')
})
window.addEventListener('hashchange', () => showPage(location.hash.slice(1) || 'inicio'))
const legacySessionNeedsLogin = clearLegacySession()
updateAuthButton()
showPage(location.hash.slice(1) || 'inicio')
if (legacySessionNeedsLogin) showToast('La sesión anterior usaba el correo como identificador. Inicia sesión de nuevo con tu nick.')
