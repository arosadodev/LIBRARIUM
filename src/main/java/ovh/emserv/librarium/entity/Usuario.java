package ovh.emserv.librarium.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * @author mit
 * @date 9/11/26 10:02
 */
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 250)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol = Rol.ROLE_USER;

    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }

    public enum Rol {ROLE_USER, ROLE_ADMIN}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convierte el Enum Rol a la autoridad que entiende Spring (ej. "ROLE_USER")[cite: 15]
        return List.of(new SimpleGrantedAuthority(this.rol.name()));
    }

    @Override
    public String getPassword() {
        return this.passwordHash; // Spring usará esto para comparar la contraseña[cite: 15]
    }

    @Override
    public String getUsername() {
        return this.email; // Usamos el email como identificador para el login[cite: 15]
    }

    // El sistema confiará en que la cuenta está activa[cite: 15]
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
