package ovh.emserv.librarium.repository;

import ovh.emserv.librarium.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author mit
 * @date 9/11/26 10:02
 */
public interface LibroRepository extends JpaRepository<Libro, Long> {
    @org.springframework.data.jpa.repository.Query("""
            SELECT DISTINCT l FROM Libro l
            LEFT JOIN l.autores a
            WHERE LOWER(l.titulo) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
            """)
    List<Libro> buscarPorTituloOAutor(String texto);

    Optional<Libro> findByOpenLibraryKey(String openLibraryKey);
}
