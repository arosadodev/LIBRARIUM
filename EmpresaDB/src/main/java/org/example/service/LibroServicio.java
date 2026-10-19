package org.example.service;

import org.example.modelo.Libro;
import org.example.repository.LibroDAO;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class LibroServicio {
    private final LibroDAO dao;
    public LibroServicio(LibroDAO dao) { this.dao = Objects.requireNonNull(dao); }
    public void insertar(Libro libro) throws SQLException { validar(libro); dao.insertar(libro); }
    public void actualizar(Libro libro) throws SQLException { validar(libro); dao.actualizar(libro); }
    public void eliminar(String key) throws SQLException { validarTexto(key, "La clave"); dao.eliminar(key); }
    public Optional<Libro> obtenerPorId(String key) throws SQLException { validarTexto(key, "La clave"); return dao.obtenerPorId(key); }
    public List<Libro> obtenerTodos() throws SQLException { return dao.obtenerTodos(); }
    public List<Libro> obtenerPorGenero(String genero) throws SQLException { validarTexto(genero, "El género"); return dao.obtenerPorGenero(genero); }
    public List<Libro> obtenerPorTitulo(String titulo) throws SQLException { validarTexto(titulo, "El título"); return dao.obtenerPorTitulo(titulo); }
    public List<Libro> obtenerPorAutor(String autor) throws SQLException { validarTexto(autor, "El autor"); return dao.obtenerPorAutor(autor); }
    private static void validar(Libro l) {
        Objects.requireNonNull(l, "El libro no puede ser nulo");
        validarTexto(l.key(), "La clave");
        validarTexto(l.workKey(), "La clave de obra");
        if (l.isbns() == null) throw new IllegalArgumentException("Los ISBN no pueden ser nulos");
    }
    private static void validarTexto(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " no puede estar vacío");
    }
}
