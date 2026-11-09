package ovh.emserv.librarium.repository;

import ovh.emserv.librarium.entity.ForoRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author mit
 * @date 9/11/26 10:02
 */
public interface ForoRespuestaRepository extends JpaRepository<ForoRespuesta, Long> {
    List<ForoRespuesta> findByHiloId(Long hiloId);
}
