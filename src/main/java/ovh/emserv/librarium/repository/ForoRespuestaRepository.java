package ovh.emserv.librarium.repository;

import ovh.emserv.librarium.entity.ForoRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForoRespuestaRepository extends JpaRepository<ForoRespuesta, Long> {
    List<ForoRespuesta> findByHiloId(Long hiloId);
}
