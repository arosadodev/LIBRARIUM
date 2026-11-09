package ovh.emserv.librarium.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class OpenLibraryService {
    private final HttpClient client = HttpClient.newHttpClient();

    public String buscar(String consulta) {
        if (consulta == null || consulta.isBlank()) {
            throw new IllegalArgumentException("La consulta no puede estar vacía");
        }
        String encoded = URLEncoder.encode(consulta, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://openlibrary.org/search.json?q=" + encoded)).header("Accept", "application/json").GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Open Library respondió con HTTP " + response.statusCode());
            }
            return response.body();
        } catch (IOException | InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("No se pudo consultar Open Library", exception);
        }
    }
}
