package ovh.emserv.librarium.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author mit
 * @date 9/11/26 10:02
 */
@Entity
@Table(name = "resenas", uniqueConstraints = @UniqueConstraint(columnNames = {"libro_id", "usuario_id"}))
@org.hibernate.annotations.Check(constraints = "puntuacion BETWEEN 1 AND 5")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resena {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer puntuacion;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }
}
