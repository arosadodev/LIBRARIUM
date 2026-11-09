INSERT INTO usuarios (username, email, password_hash, rol)
VALUES ('admin', 'admin@librarium.org', '$2y$10$JSnXt0DHCVPWps8RJZ1CkutEQpMw9yWUwnRFn6jrWX.TynJTuiedG', 'ROLE_ADMIN'),
       ('andres', 'andres@gmail.com', '$2y$10$Imsx8dxi4IkrFhsJbm2zcumQgx2TnlC0O45I1.me2fpAbfujo9lpB', 'ROLE_USER');

INSERT INTO libros (open_library_key, isbn, titulo, autor, sinopsis, ano_publicacion, portada_url)
VALUES ('/works/OL27448W', '9780451524935', '1984', 'George Orwell',
        'Novela distópica sobre el control social y el Gran Hermano.', 1949,
        'https://covers.openlibrary.org/b/id/7222246-L.jpg');

INSERT INTO resenas (libro_id, usuario_id, puntuacion, comentario)
VALUES (1, 2, 5, 'Excelente lectura.');

INSERT INTO anotaciones_guias (libro_id, usuario_id, capitulo_numero, pagina, tipo, contenido)
VALUES (1, 2, 1, 15, 'CITA', 'El Gran Hermano te vigila.');

INSERT INTO foro_hilos (libro_id, usuario_id, titulo_hilo, contenido_inicial)
VALUES (1, 2, 'Debate sobre la vigilancia', '¿Qué opináis del control en la novela?');

INSERT INTO foro_respuestas (hilo_id, usuario_id, contenido)
VALUES (1, 1, 'Es totalmente aplicable a la actualidad.');