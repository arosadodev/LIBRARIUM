package org.example.repository;

import org.example.modelo.Obra;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ObraDAO {
    void insertar(Obra obra) throws SQLException;
    void actualizar(Obra obra) throws SQLException;
    void eliminar(String key) throws SQLException;
    Optional<Obra> obtenerPorId(String key) throws SQLException;
    List<Obra> obtenerTodos() throws SQLException;
}
