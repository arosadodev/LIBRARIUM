USE librarium_db;

-- Ejecutar una sola vez sobre una base existente creada con INT.
-- Hacer una copia de seguridad antes de ejecutar en un entorno con datos reales.

ALTER TABLE anotaciones_guias
    DROP FOREIGN KEY anotaciones_guias_ibfk_1,
    DROP FOREIGN KEY anotaciones_guias_ibfk_2;

ALTER TABLE foro_hilos
    DROP FOREIGN KEY foro_hilos_ibfk_1,
    DROP FOREIGN KEY foro_hilos_ibfk_2;

ALTER TABLE foro_respuestas
    DROP FOREIGN KEY foro_respuestas_ibfk_1,
    DROP FOREIGN KEY foro_respuestas_ibfk_2;

ALTER TABLE resenas
    DROP FOREIGN KEY resenas_ibfk_1,
    DROP FOREIGN KEY resenas_ibfk_2;

ALTER TABLE usuarios MODIFY id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE libros MODIFY id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE resenas
    MODIFY id BIGINT NOT NULL AUTO_INCREMENT,
    MODIFY libro_id BIGINT NOT NULL,
    MODIFY usuario_id BIGINT NOT NULL;
ALTER TABLE anotaciones_guias
    MODIFY id BIGINT NOT NULL AUTO_INCREMENT,
    MODIFY libro_id BIGINT NOT NULL,
    MODIFY usuario_id BIGINT NOT NULL;
ALTER TABLE foro_hilos
    MODIFY id BIGINT NOT NULL AUTO_INCREMENT,
    MODIFY libro_id BIGINT NOT NULL,
    MODIFY usuario_id BIGINT NOT NULL;
ALTER TABLE foro_respuestas
    MODIFY id BIGINT NOT NULL AUTO_INCREMENT,
    MODIFY hilo_id BIGINT NOT NULL,
    MODIFY usuario_id BIGINT NOT NULL;

ALTER TABLE anotaciones_guias
    ADD CONSTRAINT anotaciones_guias_ibfk_1
        FOREIGN KEY (libro_id) REFERENCES libros (id) ON DELETE CASCADE,
    ADD CONSTRAINT anotaciones_guias_ibfk_2
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE;

ALTER TABLE foro_hilos
    ADD CONSTRAINT foro_hilos_ibfk_1
        FOREIGN KEY (libro_id) REFERENCES libros (id) ON DELETE CASCADE,
    ADD CONSTRAINT foro_hilos_ibfk_2
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE;

ALTER TABLE foro_respuestas
    ADD CONSTRAINT foro_respuestas_ibfk_1
        FOREIGN KEY (hilo_id) REFERENCES foro_hilos (id) ON DELETE CASCADE,
    ADD CONSTRAINT foro_respuestas_ibfk_2
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE;

ALTER TABLE resenas
    ADD CONSTRAINT resenas_ibfk_1
        FOREIGN KEY (libro_id) REFERENCES libros (id) ON DELETE CASCADE,
    ADD CONSTRAINT resenas_ibfk_2
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE;
