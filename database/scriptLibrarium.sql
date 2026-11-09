DROP DATABASE IF EXISTS librarium_db;
CREATE DATABASE librarium_db
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE librarium_db;

CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(250) NOT NULL,
    rol ENUM('ROLE_USER','ROLE_ADMIN') NOT NULL DEFAULT 'ROLE_USER',
    fecha_registro DATETIME default CURRENT_TIMESTAMP
);

CREATE TABLE libros (
    id INT AUTO_INCREMENT PRIMARY KEY,
    open_library_key VARCHAR(100),
    isbn VARCHAR(20),
    titulo VARCHAR(255) NOT NULL,
    autor VARCHAR(255) NOT NULL,
    sinopsis TEXT,
    ano_publicacion INT,
    portada_url VARCHAR(500),
    fecha_registro DATETIME default CURRENT_TIMESTAMP
);

CREATE TABLE resenas (
    id INT AUTO_INCREMENT PRIMARY KEY ,
    libro_id INT NOT NULL,
    usuario_id INT NOT NULL,
    puntuacion INT NOT NULL,
    comentario TEXT,
    fecha_registro DATETIME default CURRENT_TIMESTAMP,
    FOREIGN KEY (libro_id) REFERENCES libros(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);