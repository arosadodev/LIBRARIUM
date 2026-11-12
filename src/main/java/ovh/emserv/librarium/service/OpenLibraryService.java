package ovh.emserv.librarium.service;

import ovh.emserv.librarium.dto.OpenLibraryImportarDTO;
import ovh.emserv.librarium.dto.OpenLibraryLibroDTO;
import ovh.emserv.librarium.entity.Libro;
import ovh.emserv.librarium.entity.Autor;
import ovh.emserv.librarium.repository.AutorRepository;
import ovh.emserv.librarium.repository.LibroRepository;
import ovh.emserv.librarium.exception.PeticionInvalidaException;
import ovh.emserv.librarium.exception.ServicioExternoException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class OpenLibraryService {
    private static final String SEARCH_URL = "https://openlibrary.org/search.json?q=";

    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;

    public OpenLibraryService(ObjectMapper objectMapper, LibroRepository libroRepository,
                              AutorRepository autorRepository) {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = objectMapper;
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public List<OpenLibraryLibroDTO> buscar(String consulta) {
        if (consulta == null || consulta.isBlank()) {
            throw new PeticionInvalidaException("La consulta no puede estar vacía");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SEARCH_URL + URLEncoder.encode(consulta.trim(), StandardCharsets.UTF_8)))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new ServicioExternoException("Open Library respondió con HTTP " + response.statusCode());
            }
            return convertirResultados(response.body());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ServicioExternoException("La consulta a Open Library fue interrumpida", exception);
        } catch (IOException exception) {
            throw new ServicioExternoException("No se pudo consultar Open Library", exception);
        }
    }

    public Libro importar(OpenLibraryImportarDTO datos) {
        return libroRepository.findByOpenLibraryKey(datos.key())
                .map(existente -> {
                    if (existente.getSinopsis() == null || existente.getSinopsis().isBlank()) {
                        String sinopsis = datos.sinopsis();
                        if (sinopsis == null || sinopsis.isBlank()) {
                            sinopsis = obtenerSinopsis(datos.key());
                        }
                        if (sinopsis != null && !sinopsis.isBlank()) {
                            existente.setSinopsis(sinopsis);
                            return libroRepository.save(existente);
                        }
                    }
                    return existente;
                })
                .orElseGet(() -> libroRepository.save(crearLibro(datos)));
    }

    public String obtenerSinopsis(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        String path = key.startsWith("/") ? key : "/" + key;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://openlibrary.org" + path + ".json"))
                .timeout(Duration.ofSeconds(6))
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return extraerSinopsisDeDetalle(response.body());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } catch (Exception ignored) {
        }
        return null;
    }

    private String extraerSinopsisDeDetalle(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            return extraerSinopsis(root);
        } catch (Exception e) {
            return null;
        }
    }

    private String extraerSinopsis(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        JsonNode desc = node.get("description");
        if (desc != null && !desc.isNull()) {
            if (desc.isTextual() && !desc.asText().isBlank()) {
                return desc.asText().trim();
            }
            if (desc.isObject() && desc.has("value") && !desc.get("value").asText().isBlank()) {
                return desc.get("value").asText().trim();
            }
            if (desc.isArray() && desc.size() > 0) {
                String first = desc.get(0).asText();
                if (!first.isBlank()) {
                    return first.trim();
                }
            }
        }

        JsonNode firstSentence = node.get("first_sentence");
        if (firstSentence != null && !firstSentence.isNull()) {
            if (firstSentence.isArray() && firstSentence.size() > 0) {
                String sentence = firstSentence.get(0).asText();
                if (!sentence.isBlank()) {
                    return sentence.trim();
                }
            } else if (firstSentence.isTextual() && !firstSentence.asText().isBlank()) {
                return firstSentence.asText().trim();
            } else if (firstSentence.isObject() && firstSentence.has("value") && !firstSentence.get("value").asText().isBlank()) {
                return firstSentence.get("value").asText().trim();
            }
        }

        return null;
    }

    private List<OpenLibraryLibroDTO> convertirResultados(String json) throws IOException {
        JsonNode docs = objectMapper.readTree(json).path("docs");
        List<OpenLibraryLibroDTO> resultados = new ArrayList<>();

        for (JsonNode doc : docs) {
            String key = textOrNull(doc, "key");
            String title = textOrNull(doc, "title");
            if (key == null || title == null) {
                continue;
            }

            List<String> authors = new ArrayList<>();
            doc.path("author_name").forEach(author -> authors.add(author.asText()));
            List<String> isbns = new ArrayList<>();
            doc.path("isbn").forEach(isbn -> isbns.add(isbn.asText()));

            Integer coverId = doc.has("cover_i") ? doc.get("cover_i").asInt() : null;
            String coverUrl = coverId == null
                    ? null
                    : "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg";

            String sinopsis = extraerSinopsis(doc);

            resultados.add(new OpenLibraryLibroDTO(
                    key,
                    title,
                    authors,
                    isbns.isEmpty() ? null : isbns.get(0),
                    doc.has("first_publish_year") ? doc.get("first_publish_year").asInt() : null,
                    coverUrl,
                    sinopsis
            ));
        }
        return resultados;
    }

    private Libro crearLibro(OpenLibraryImportarDTO datos) {
        if (datos.title() == null || datos.title().isBlank()) {
            throw new PeticionInvalidaException("El título del libro es obligatorio");
        }

        Libro libro = new Libro();
        libro.setOpenLibraryKey(datos.key());
        libro.setTitulo(datos.title());
        List<String> nombres = datos.authors() == null || datos.authors().isEmpty()
                ? List.of("Autor desconocido")
                : datos.authors().stream().filter(nombre -> nombre != null && !nombre.isBlank()).distinct().toList();
        nombres.forEach(nombre -> libro.getAutores().add(autorRepository.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> autorRepository.save(new Autor(null, nombre)))));
        libro.setIsbn(datos.isbn());
        libro.setAnoPublicacion(datos.firstPublishYear());
        libro.setPortadaUrl(datos.coverUrl());

        String sinopsis = datos.sinopsis();
        if ((sinopsis == null || sinopsis.isBlank()) && datos.key() != null && !datos.key().isBlank()) {
            sinopsis = obtenerSinopsis(datos.key());
        }
        libro.setSinopsis(sinopsis);
        return libro;
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() || value.asText().isBlank() ? null : value.asText();
    }
}
