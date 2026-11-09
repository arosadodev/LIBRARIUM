package ovh.emserv.librarium.repository;

import ovh.emserv.librarium.entity.Anotacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnotacionRepository extends JpaRepository<Anotacion, Long> {
    List<Anotacion> findByLibroId(Long libroId);
}
