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
@Table(name = "libros")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "open_library_key", length = 100, unique = true)
    private String openLibraryKey;

    @Column(length = 20)
    private String isbn;

    @Column(nullable = false, length = 255)
    private String titulo;

    @ManyToMany
    @JoinTable(name = "libros_autores",
            joinColumns = @JoinColumn(name = "libro_id"),
            inverseJoinColumns = @JoinColumn(name = "autor_id"))
    private java.util.Set<Autor> autores = new java.util.LinkedHashSet<>();

    @Column(columnDefinition = "TEXT")
    private String sinopsis;

    @Column(name = "ano_publicacion")
    private Integer anoPublicacion;

    @Column(name = "portada_url", length = 500)
    private String portadaUrl;

    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }
}
