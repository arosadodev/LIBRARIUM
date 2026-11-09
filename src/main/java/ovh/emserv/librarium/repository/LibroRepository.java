package ovh.emserv.librarium.repository;

import ovh.emserv.librarium.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author mit
 * @date 9/11/26 10:02
 */
public interface LibroRepository extends JpaRepository<Libro, Long> {
    List<Libro> findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(String titulo, String autor);
}
