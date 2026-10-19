package org.example.repository;

import org.example.conexion.Conexion;
import org.example.modelo.Libro;
import org.example.modelo.LibroIsbn;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class LibroDAOJdbc implements LibroDAO {
    private static final String SELECT = """
            SELECT e.key AS edition_key, e.data AS edition_data, e.work_key,
                   COALESCE(json_extract(w.data, '$.title'), w.data) AS work_title,
                   aw.author_key
            FROM editions e
            JOIN works w ON w.key=e.work_key
            LEFT JOIN author_works aw ON aw.work_key=w.key
            """;

    public void insertar(Libro libro) throws SQLException {
        try (Connection c = Conexion.abrir()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement s = c.prepareStatement(
                        "INSERT INTO editions (type,key,work_key,data) VALUES ('/type/edition',?,?,?)")) {
                    s.setString(1, libro.key()); s.setString(2, libro.workKey()); s.setString(3, libro.data()); s.executeUpdate();
                }
                insertarIsbn(c, libro);
                c.commit();
            } catch (SQLException error) {
                c.rollback();
                throw error;
            }
        }
    }

    public void actualizar(Libro libro) throws SQLException {
        try (Connection c = Conexion.abrir()) {
            c.setAutoCommit(false);
            try (PreparedStatement s = c.prepareStatement("UPDATE editions SET work_key=?,data=? WHERE key=?");
                 PreparedStatement borrar = c.prepareStatement("DELETE FROM edition_isbns WHERE edition_key=?");
                 PreparedStatement insertar = c.prepareStatement("INSERT INTO edition_isbns (edition_key,isbn) VALUES (?,?)")) {
                s.setString(1, libro.workKey()); s.setString(2, libro.data()); s.setString(3, libro.key()); s.executeUpdate();
                borrar.setString(1, libro.key()); borrar.executeUpdate();
                for (LibroIsbn isbn : libro.isbns()) {
                    insertar.setString(1, libro.key()); insertar.setString(2, isbn.isbn()); insertar.addBatch();
                }
                insertar.executeBatch(); c.commit();
            } catch (SQLException e) { c.rollback(); throw e; }
        }
    }

    public void eliminar(String key) throws SQLException {
        try (Connection c = Conexion.abrir(); PreparedStatement s = c.prepareStatement("DELETE FROM editions WHERE key=?")) {
            s.setString(1, key); s.executeUpdate();
        }
    }

    public Optional<Libro> obtenerPorId(String key) throws SQLException {
        return consultar(SELECT + "WHERE e.key=?", key).stream().findFirst();
    }

    public List<Libro> obtenerTodos() throws SQLException {
        return consultar(SELECT + "ORDER BY e.key");
    }

    public List<Libro> obtenerPorGenero(String genero) throws SQLException {
        return consultar(SELECT + "WHERE e.data LIKE ? ORDER BY e.key", "%" + genero + "%");
    }

    public List<Libro> obtenerPorTitulo(String titulo) throws SQLException {
        return consultar(SELECT + "WHERE e.data LIKE ? OR w.data LIKE ? ORDER BY e.key", "%" + titulo + "%", "%" + titulo + "%");
    }

    public List<Libro> obtenerPorAutor(String autor) throws SQLException {
        return consultar(SELECT + "JOIN authors a ON a.key=aw.author_key WHERE a.data LIKE ? ORDER BY e.key", "%" + autor + "%");
    }

    private static void insertarIsbn(Connection c, Libro libro) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("INSERT INTO edition_isbns (edition_key,isbn) VALUES (?,?)")) {
            for (LibroIsbn isbn : libro.isbns()) {
                s.setString(1, libro.key()); s.setString(2, isbn.isbn()); s.addBatch();
            }
            s.executeBatch();
        }
    }

    private static List<Libro> consultar(String sql, String... parametros) throws SQLException {
        try (Connection c = Conexion.abrir(); PreparedStatement s = c.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) s.setString(i + 1, parametros[i]);
            try (ResultSet r = s.executeQuery()) {
                List<Libro> libros = new ArrayList<>();
                while (r.next()) libros.add(mapear(c, r));
                return libros;
            }
        }
    }

    private static Libro mapear(Connection c, ResultSet r) throws SQLException {
        String key = r.getString("edition_key");
        List<LibroIsbn> isbns = new ArrayList<>();
        try (PreparedStatement s = c.prepareStatement("SELECT isbn FROM edition_isbns WHERE edition_key=?")) {
            s.setString(1, key);
            try (ResultSet isbn = s.executeQuery()) {
                while (isbn.next()) isbns.add(new LibroIsbn(isbn.getString("isbn")));
            }
        }
        return new Libro(key, r.getString("edition_data"), r.getString("author_key"),
                r.getString("work_key"), r.getString("work_title"), isbns);
    }
}
