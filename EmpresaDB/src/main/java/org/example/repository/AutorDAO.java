package org.example.repository;

import org.example.modelo.Autor;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface AutorDAO {
    void insertar(Autor autor) throws SQLException;
    void actualizar(Autor autor) throws SQLException;
    void eliminar(String key) throws SQLException;
    Optional<Autor> obtenerPorId(String key) throws SQLException;
    List<Autor> obtenerTodos() throws SQLException;
    List<Autor> obtenerPorNombre(String nombre) throws SQLException;
}
