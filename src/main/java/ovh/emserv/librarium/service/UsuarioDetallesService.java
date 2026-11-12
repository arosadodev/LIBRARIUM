package ovh.emserv.librarium.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ovh.emserv.librarium.repository.UsuarioRepository;

/**
 * @author mit
 * @date 9/11/26 20:14
 */
@Service
public class UsuarioDetallesService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioDetallesService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByUsername(username.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con nick: " + username));
    }
}
