package org.example.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Conexion {
    private static String ruta = System.getProperty("librarium.db", "../libros.db");

    private Conexion() {}

    public static void establecerRuta(Path nuevaRuta) {
        ruta = nuevaRuta.toAbsolutePath().toString();
    }

    public static Connection abrir() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException error) {
            throw new SQLException("No se encontró el driver SQLite JDBC", error);
        }
        String url = "jdbc:sqlite:" + Paths.get(ruta).toAbsolutePath().normalize();
        Connection connection = DriverManager.getConnection(url);
        try (var statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS authors (
                        type TEXT, key TEXT PRIMARY KEY, revision INTEGER,
                        last_modified TEXT, data TEXT NOT NULL
                    )""");
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS works (
                        type TEXT, key TEXT PRIMARY KEY, revision INTEGER,
                        last_modified TEXT, data TEXT NOT NULL
                    )""");
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS author_works (
                        author_key TEXT NOT NULL, work_key TEXT NOT NULL,
                        PRIMARY KEY (author_key, work_key),
                        FOREIGN KEY (author_key) REFERENCES authors(key),
                        FOREIGN KEY (work_key) REFERENCES works(key)
                    )""");
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS editions (
                        type TEXT, key TEXT PRIMARY KEY, revision INTEGER,
                        last_modified TEXT, work_key TEXT NOT NULL, data TEXT NOT NULL,
                        FOREIGN KEY (work_key) REFERENCES works(key)
                    )""");
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS edition_isbns (
                        edition_key TEXT NOT NULL, isbn TEXT NOT NULL,
                        PRIMARY KEY (edition_key, isbn), UNIQUE (isbn),
                        FOREIGN KEY (edition_key) REFERENCES editions(key) ON DELETE CASCADE
                    )""");
        }
        return connection;
    }
}
