package org.example.modelo;

import java.util.List;

public record Libro(
        String key,
        String titulo,
        String autorKey,
        String workKey,
        String data,
        List<LibroIsbn> isbns
) {
}
