USE librarium_db;

CREATE TABLE IF NOT EXISTS autores
(
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS libros_autores
(
    libro_id BIGINT NOT NULL,
    autor_id BIGINT NOT NULL,
    PRIMARY KEY (libro_id, autor_id),
    FOREIGN KEY (libro_id) REFERENCES libros (id) ON DELETE CASCADE,
    FOREIGN KEY (autor_id) REFERENCES autores (id) ON DELETE CASCADE
);

INSERT IGNORE INTO autores (nombre)
SELECT DISTINCT TRIM(autor)
FROM libros
WHERE autor IS NOT NULL AND TRIM(autor) <> '';

INSERT IGNORE INTO libros_autores (libro_id, autor_id)
SELECT l.id, a.id
FROM libros l
JOIN autores a ON a.nombre = TRIM(l.autor)
WHERE l.autor IS NOT NULL AND TRIM(l.autor) <> '';

ALTER TABLE libros DROP COLUMN autor;
