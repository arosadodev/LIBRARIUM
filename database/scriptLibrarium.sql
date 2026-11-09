DROP DATABASE IF EXISTS librarium_db;
CREATE DATABASE librarium_db
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE librarium_db;

CREATE TABLE usuarios
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)                     NOT NULL UNIQUE,
    email          VARCHAR(100)                    NOT NULL UNIQUE,
    password_hash  VARCHAR(250)                    NOT NULL,
    rol            ENUM ('ROLE_USER','ROLE_ADMIN') NOT NULL DEFAULT 'ROLE_USER',
    fecha_registro DATETIME                                 default CURRENT_TIMESTAMP
);

CREATE TABLE libros
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    open_library_key VARCHAR(100) UNIQUE,
    isbn             VARCHAR(20),
    titulo           VARCHAR(255) NOT NULL,
    sinopsis         TEXT,
    ano_publicacion  INT,
    portada_url      VARCHAR(500),
    fecha_registro   DATETIME default CURRENT_TIMESTAMP
);

CREATE TABLE autores
(
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE libros_autores
(
    libro_id BIGINT NOT NULL,
    autor_id BIGINT NOT NULL,
    PRIMARY KEY (libro_id, autor_id),
    FOREIGN KEY (libro_id) REFERENCES libros (id) ON DELETE CASCADE,
    FOREIGN KEY (autor_id) REFERENCES autores (id) ON DELETE CASCADE
);

CREATE TABLE resenas
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    libro_id       BIGINT NOT NULL,
    usuario_id     BIGINT NOT NULL,
    puntuacion     INT NOT NULL CHECK (puntuacion BETWEEN 1 AND 5),
    comentario     TEXT,
    fecha_registro DATETIME default CURRENT_TIMESTAMP,
    FOREIGN KEY (libro_id) REFERENCES libros (id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
);

CREATE TABLE anotaciones_guias
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    libro_id        BIGINT                                   NOT NULL,
    usuario_id      BIGINT                                   NOT NULL,
    capitulo_numero INT,
    pagina          INT,
    tipo            ENUM ('CITA', 'ANALISIS', 'REFERENCIA') NOT NULL,
    contenido       TEXT                                    NOT NULL,
    fecha_registro  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (libro_id) REFERENCES libros (id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
);

CREATE TABLE foro_hilos
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    libro_id          BIGINT         NOT NULL,
    usuario_id       BIGINT         NOT NULL,
    titulo_hilo       VARCHAR(200) NOT NULL,
    contenido_inicial TEXT         NOT NULL,
    fecha_registro    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (libro_id) REFERENCES libros (id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
);

CREATE TABLE foro_respuestas
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    hilo_id        BIGINT NOT NULL,
    usuario_id     BIGINT NOT NULL,
    contenido      TEXT NOT NULL,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (hilo_id) REFERENCES foro_hilos (id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
);