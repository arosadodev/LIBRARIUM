package ovh.emserv.librarium.repository;

import ovh.emserv.librarium.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author mit
 * @date 9/11/26 10:02
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
}
