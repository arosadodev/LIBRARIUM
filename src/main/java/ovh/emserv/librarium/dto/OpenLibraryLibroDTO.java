package ovh.emserv.librarium.dto;

import java.util.List;

public record OpenLibraryLibroDTO(
        String key,
        String title,
        List<String> authors,
        String isbn,
        Integer firstPublishYear,
        String coverUrl,
        String sinopsis
) {
}
