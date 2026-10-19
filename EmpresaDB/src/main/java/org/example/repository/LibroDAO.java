package org.example.repository;

import org.example.modelo.Libro;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface LibroDAO {
    void insertar(Libro libro) throws SQLException;
    void actualizar(Libro libro) throws SQLException;
    void eliminar(String key) throws SQLException;
    Optional<Libro> obtenerPorId(String key) throws SQLException;
    List<Libro> obtenerTodos() throws SQLException;
    List<Libro> obtenerPorGenero(String genero) throws SQLException;
    List<Libro> obtenerPorTitulo(String titulo) throws SQLException;
    List<Libro> obtenerPorAutor(String autor) throws SQLException;
}
