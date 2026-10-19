package org.example.repository;

import org.example.conexion.Conexion;
import org.example.modelo.Obra;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ObraDAOJdbc implements ObraDAO {
    public void insertar(Obra obra) throws SQLException {
        ejecutar("INSERT INTO works (type,key,data) VALUES ('/type/work',?,?)", obra, false);
        actualizarAutores(obra);
    }
    public void actualizar(Obra obra) throws SQLException {
        ejecutar("UPDATE works SET data=? WHERE key=?", obra, true);
        actualizarAutores(obra);
    }
    private void ejecutar(String sql, Obra obra, boolean update) throws SQLException {
        try (Connection c = Conexion.abrir(); PreparedStatement s = c.prepareStatement(sql)) {
            if (update) { s.setString(1, obra.data()); s.setString(2, obra.key()); }
            else { s.setString(1, obra.key()); s.setString(2, obra.data()); }
            s.executeUpdate();
        }
    }
    private void actualizarAutores(Obra obra) throws SQLException {
            try (Connection c = Conexion.abrir()) {
                c.setAutoCommit(false);
                try (PreparedStatement borrar = c.prepareStatement("DELETE FROM author_works WHERE work_key=?");
                     PreparedStatement insertar = c.prepareStatement("INSERT INTO author_works (author_key,work_key) VALUES (?,?)")) {
                    borrar.setString(1, obra.key()); borrar.executeUpdate();
                    for (String autor : obra.autores()) {
                        insertar.setString(1, autor); insertar.setString(2, obra.key()); insertar.addBatch();
                    }
                    insertar.executeBatch(); c.commit();
                } catch (SQLException e) { c.rollback(); throw e; }
        }
    }
    public void eliminar(String key) throws SQLException {
        try (Connection c = Conexion.abrir(); PreparedStatement s = c.prepareStatement("DELETE FROM works WHERE key=?")) {
            s.setString(1, key); s.executeUpdate();
        }
    }
    public Optional<Obra> obtenerPorId(String key) throws SQLException {
        try (Connection c = Conexion.abrir(); PreparedStatement s = c.prepareStatement("SELECT key,data FROM works WHERE key=?")) {
            s.setString(1, key);
            try (ResultSet r = s.executeQuery()) {
                return r.next() ? Optional.of(mapear(c, r)) : Optional.empty();
            }
        }
    }
    public List<Obra> obtenerTodos() throws SQLException {
        List<Obra> resultado = new ArrayList<>();
        try (Connection c = Conexion.abrir(); PreparedStatement s = c.prepareStatement("SELECT key,data FROM works ORDER BY key");
             ResultSet r = s.executeQuery()) {
            while (r.next()) resultado.add(mapear(c, r));
        }
        return resultado;
    }
    private static Obra mapear(Connection c, ResultSet r) throws SQLException {
        List<String> autores = new ArrayList<>();
        try (PreparedStatement s = c.prepareStatement("SELECT author_key FROM author_works WHERE work_key=?")) {
            s.setString(1, r.getString(1));
            try (ResultSet a = s.executeQuery()) { while (a.next()) autores.add(a.getString(1)); }
        }
        return new Obra(r.getString(1), r.getString(2), autores);
    }
}
