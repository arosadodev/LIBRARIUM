package org.example.service;

import org.example.modelo.Autor;
import org.example.repository.AutorDAO;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class AutorServicio {
    private final AutorDAO dao;
    public AutorServicio(AutorDAO dao) { this.dao = Objects.requireNonNull(dao); }
    public void insertar(Autor autor) throws SQLException { validar(autor); dao.insertar(autor); }
    public void actualizar(Autor autor) throws SQLException { validar(autor); dao.actualizar(autor); }
    public void eliminar(String key) throws SQLException { validarTexto(key, "La clave"); dao.eliminar(key); }
    public Optional<Autor> obtenerPorId(String key) throws SQLException { validarTexto(key, "La clave"); return dao.obtenerPorId(key); }
    public List<Autor> obtenerTodos() throws SQLException { return dao.obtenerTodos(); }
    public List<Autor> obtenerPorNombre(String nombre) throws SQLException { validarTexto(nombre, "El nombre"); return dao.obtenerPorNombre(nombre); }
    private static void validar(Autor a) { Objects.requireNonNull(a); validarTexto(a.key(), "La clave"); validarTexto(a.nombre(), "El nombre"); }
    private static void validarTexto(String value, String field) { if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " no puede estar vacío"); }
}
