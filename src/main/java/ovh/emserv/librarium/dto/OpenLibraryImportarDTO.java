package ovh.emserv.librarium.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record OpenLibraryImportarDTO(
        @NotBlank String key,
        String title,
        List<String> authors,
        String isbn,
        Integer firstPublishYear,
        String coverUrl
) {
}
