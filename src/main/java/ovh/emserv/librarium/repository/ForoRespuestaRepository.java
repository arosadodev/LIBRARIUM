package ovh.emserv.librarium.repository;

import ovh.emserv.librarium.entity.ForoRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author mit
 * @date 9/11/26 10:02
 */
public interface ForoRespuestaRepository extends JpaRepository<ForoRespuesta, Long> {
    Page<ForoRespuesta> findByHiloId(Long hiloId, Pageable pageable);
}
