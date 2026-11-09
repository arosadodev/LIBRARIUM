package ovh.emserv.librarium.repository;

import ovh.emserv.librarium.entity.ForoHilo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author mit
 * @date 9/11/26 10:02
 */
public interface ForoHiloRepository extends JpaRepository<ForoHilo, Long> {
    List<ForoHilo> findByLibroId(Long libroId);
}
