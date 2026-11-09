package ovh.emserv.librarium.repository;

import ovh.emserv.librarium.entity.ForoHilo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForoHiloRepository extends JpaRepository<ForoHilo, Long> {
    List<ForoHilo> findByLibroId(Long libroId);
}
