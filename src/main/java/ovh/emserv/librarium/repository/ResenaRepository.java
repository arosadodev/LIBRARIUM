package ovh.emserv.librarium.repository;

import ovh.emserv.librarium.entity.Resena;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author mit
 * @date 9/11/26 10:02
 */
public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByLibroId(Long libroId);

    List<Resena> findByUsuarioId(Long usuarioId);

    boolean existsByLibroIdAndUsuarioId(Long libroId, Long usuarioId);
}
