package ovh.emserv.librarium.controller;

import jakarta.validation.Valid;
import ovh.emserv.librarium.dto.OpenLibraryImportarDTO;
import ovh.emserv.librarium.dto.OpenLibraryLibroDTO;
import ovh.emserv.librarium.entity.Libro;
import ovh.emserv.librarium.service.LibroService;
import ovh.emserv.librarium.service.OpenLibraryService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author mit
 * @date 9/11/26 10:02
 */
@RestController
@RequestMapping("/api/libros")
public class LibroController {
    private final LibroService service;
    private final OpenLibraryService openLibrary;

    public LibroController(LibroService service, OpenLibraryService openLibrary) {
        this.service = service;
        this.openLibrary = openLibrary;
    }

    @GetMapping
    public List<Libro> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Libro obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/buscar")
    public List<Libro> buscar(@RequestParam String q) {
        return service.buscar(q);
    }

    @GetMapping(value = "/buscar-externo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OpenLibraryLibroDTO>> buscarExterno(@RequestParam String q) {
        return ResponseEntity.ok(openLibrary.buscar(q));
    }

    @PostMapping("/importar")
    public ResponseEntity<Libro> importar(@Valid @RequestBody OpenLibraryImportarDTO datos) {
        return ResponseEntity.ok(openLibrary.importar(datos));
    }
}
