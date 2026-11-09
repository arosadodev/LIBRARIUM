package ovh.emserv.librarium.repository;

import ovh.emserv.librarium.entity.Resena;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByLibroId(Long libroId);
}
