package org.example.repository;

import org.example.conexion.Conexion;
import org.example.modelo.Autor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AutorDAOJdbc implements AutorDAO {
    public void insertar(Autor autor) throws SQLException {
        ejecutar("INSERT INTO authors (type,key,data) VALUES ('/type/author',?,?)", autor, false);
    }

    public void actualizar(Autor autor) throws SQLException {
        ejecutar("UPDATE authors SET data=? WHERE key=?", autor, true);
    }

    private void ejecutar(String sql, Autor autor, boolean update) throws SQLException {
        try (Connection c = Conexion.abrir(); PreparedStatement s = c.prepareStatement(sql)) {
            if (update) { s.setString(1, autor.data()); s.setString(2, autor.key()); }
            else { s.setString(1, autor.key()); s.setString(2, autor.data()); }
            s.executeUpdate();
        }
    }

    public void eliminar(String key) throws SQLException {
        try (Connection c = Conexion.abrir(); PreparedStatement s = c.prepareStatement("DELETE FROM authors WHERE key=?")) {
            s.setString(1, key); s.executeUpdate();
        }
    }

    public Optional<Autor> obtenerPorId(String key) throws SQLException {
        try (Connection c = Conexion.abrir(); PreparedStatement s = c.prepareStatement("SELECT key,data FROM authors WHERE key=?")) {
            s.setString(1, key);
            try (ResultSet r = s.executeQuery()) { return r.next() ? Optional.of(mapear(r)) : Optional.empty(); }
        }
    }

    public List<Autor> obtenerTodos() throws SQLException {
        return consultar("SELECT key,data FROM authors ORDER BY key", null);
    }

    public List<Autor> obtenerPorNombre(String nombre) throws SQLException {
        return consultar("SELECT key,data FROM authors WHERE data LIKE ? ORDER BY key", "%" + nombre + "%");
    }

    private List<Autor> consultar(String sql, String parametro) throws SQLException {
        try (Connection c = Conexion.abrir(); PreparedStatement s = c.prepareStatement(sql)) {
            if (parametro != null) s.setString(1, parametro);
            try (ResultSet r = s.executeQuery()) {
                List<Autor> resultado = new ArrayList<>();
                while (r.next()) resultado.add(mapear(r));
                return resultado;
            }
        }
    }

    private static Autor mapear(ResultSet r) throws SQLException {
        String data = r.getString("data");
        return new Autor(r.getString("key"), data, data);
    }
}
