package org.example;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.conexion.Conexion;
import org.example.modelo.Autor;
import org.example.modelo.Libro;
import org.example.modelo.LibroIsbn;
import org.example.modelo.Obra;
import org.example.repository.*;
import org.example.service.AutorServicio;
import org.example.service.LibroServicio;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

class AplicacionBase extends Application {
    private final LibroServicio libros = new LibroServicio(new LibroDAOJdbc());
    private final AutorServicio autores = new AutorServicio(new AutorDAOJdbc());
    private final ObraDAO obras = new ObraDAOJdbc();
    private final TableView<Autor> tablaAutores = new TableView<>();
    private final TableView<Obra> tablaObras = new TableView<>();
    private final TableView<Libro> tablaEdiciones = new TableView<>();
    private final Label estado = new Label();
    private final TextField autorKey = new TextField(), obraKey = new TextField(), obraAutores = new TextField();
    private final TextArea autorData = new TextArea(), obraData = new TextArea(), edicionData = new TextArea();
    private final TextField edicionKey = new TextField(), edicionObra = new TextField(), edicionIsbn = new TextField();

    @Override public void start(Stage stage) {
        configurarTablas();
        Button abrir = new Button("Abrir base descargada");
        Button descargar = new Button("Descargar base por URL");
        Button actualizar = new Button("Actualizar");
        abrir.setOnAction(e -> abrirBase(stage));
        descargar.setOnAction(e -> descargarBase(stage));
        actualizar.setOnAction(e -> cargarDatos());

        VBox cabecera = new VBox(8, new Label("LIBRARIUM - catálogo Open Library"),
                new HBox(8, abrir, descargar, actualizar), estado);
        cabecera.setPadding(new Insets(14));
        TabPane pestañas = new TabPane();
        pestañas.getTabs().addAll(List.of(
                pestana("Autores", tablaAutores, formularioAutor()),
                pestana("Obras", tablaObras, formularioObra()),
                pestana("Ediciones e ISBN", tablaEdiciones, formularioEdicion())));
        BorderPane root = new BorderPane(pestañas);
        root.setTop(cabecera);
        root.setPadding(new Insets(10));
        cargarDatos();
        stage.setTitle("LIBRARIUM");
        stage.setScene(new Scene(root, 1150, 720));
        stage.show();
    }

    private Tab pestana(String titulo, TableView<?> tabla, VBox formulario) {
        SplitPane contenido = new SplitPane(tabla, formulario);
        contenido.setDividerPositions(.68);
        Tab tab = new Tab(titulo, contenido);
        tab.setClosable(false);
        return tab;
    }

    private VBox formularioAutor() {
        Button guardar = new Button("Guardar autor"), borrar = new Button("Borrar seleccionado"), nuevo = new Button("Limpiar");
        guardar.setOnAction(e -> ejecutar(() -> {
            Autor autor = new Autor(autorKey.getText().trim(), autorData.getText(), autorData.getText());
            if (autores.obtenerPorId(autor.key()).isPresent()) autores.actualizar(autor); else autores.insertar(autor);
        }));
        borrar.setOnAction(e -> ejecutar(() -> autores.eliminar(autorKey.getText().trim())));
        nuevo.setOnAction(e -> { autorKey.clear(); autorData.clear(); });
        tablaAutores.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            if (b != null) { autorKey.setText(b.key()); autorData.setText(b.data()); }
        });
        return formulario("Clave Open Library", autorKey, "JSON del autor", autorData,
                new HBox(6, guardar, borrar, nuevo));
    }

    private VBox formularioObra() {
        Button guardar = new Button("Guardar obra"), borrar = new Button("Borrar seleccionado"), nuevo = new Button("Limpiar");
        guardar.setOnAction(e -> ejecutar(() -> {
            List<String> autoresObra = Arrays.stream(obraAutores.getText().split(","))
                    .map(String::trim).filter(s -> !s.isBlank()).toList();
            Obra obra = new Obra(obraKey.getText().trim(), obraData.getText(), autoresObra);
            if (obras.obtenerPorId(obra.key()).isPresent()) obras.actualizar(obra); else obras.insertar(obra);
        }));
        borrar.setOnAction(e -> ejecutar(() -> obras.eliminar(obraKey.getText().trim())));
        nuevo.setOnAction(e -> { obraKey.clear(); obraAutores.clear(); obraData.clear(); });
        tablaObras.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            if (b != null) {
                obraKey.setText(b.key()); obraAutores.setText(String.join(", ", b.autores())); obraData.setText(b.data());
            }
        });
        return formulario("Clave Open Library", obraKey, "Claves de autor separadas por coma", obraAutores,
                "JSON de la obra", obraData,
                new HBox(6, guardar, borrar, nuevo));
    }

    private VBox formularioEdicion() {
        Button guardar = new Button("Guardar edición"), borrar = new Button("Borrar seleccionado"), nuevo = new Button("Limpiar");
        guardar.setOnAction(e -> ejecutar(() -> {
            List<LibroIsbn> isbns = Arrays.stream(edicionIsbn.getText().split(","))
                    .map(String::trim).filter(s -> !s.isBlank()).map(LibroIsbn::new).toList();
            Libro libro = new Libro(edicionKey.getText().trim(), edicionData.getText(),
                    "", edicionObra.getText().trim(), edicionData.getText(), isbns);
            if (libros.obtenerPorId(libro.key()).isPresent()) libros.actualizar(libro); else libros.insertar(libro);
        }));
        borrar.setOnAction(e -> ejecutar(() -> libros.eliminar(edicionKey.getText().trim())));
        nuevo.setOnAction(e -> { edicionKey.clear(); edicionObra.clear(); edicionIsbn.clear(); edicionData.clear(); });
        tablaEdiciones.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            if (b != null) {
                edicionKey.setText(b.key()); edicionObra.setText(b.workKey());
                edicionData.setText(b.data());
                edicionIsbn.setText(b.isbns().stream().map(LibroIsbn::isbn).reduce((x, y) -> x + ", " + y).orElse(""));
            }
        });
        return formulario("Clave de edición", edicionKey, "Clave de obra", edicionObra,
                "ISBN separados por coma", edicionIsbn, "JSON de la edición", edicionData,
                new HBox(6, guardar, borrar, nuevo));
    }

    private VBox formulario(String l1, TextField f1, String l2, TextArea a2, HBox botones) {
        return new VBox(8, new Label(l1), f1, new Label(l2), a2, botones);
    }

    private VBox formulario(String l1, TextField f1, String l2, TextField f2, String l3, TextArea a3,
                            HBox botones) {
        return new VBox(8, new Label(l1), f1, new Label(l2), f2, new Label(l3), a3, botones);
    }

    private VBox formulario(String l1, TextField f1, String l2, TextField f2, String l3, TextField f3,
                            String l4, TextArea a4, HBox botones) {
        return new VBox(8, new Label(l1), f1, new Label(l2), f2, new Label(l3), f3,
                new Label(l4), a4, botones);
    }

    private void configurarTablas() {
        TableColumn<Autor, String> ak = columna("Clave", Autor::key), ad = columna("JSON", Autor::data);
        tablaAutores.getColumns().addAll(List.of(ak, ad)); tablaAutores.setPlaceholder(new Label("No hay autores cargados."));
        TableColumn<Obra, String> ok = columna("Clave", Obra::key), od = columna("JSON", Obra::data);
        tablaObras.getColumns().addAll(List.of(ok, od)); tablaObras.setPlaceholder(new Label("No hay obras cargadas."));
        TableColumn<Libro, String> ek = columna("Edición", Libro::key), ew = columna("Obra", Libro::workKey);
        TableColumn<Libro, String> ei = new TableColumn<>("ISBN");
        ei.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isbns().stream()
                .map(LibroIsbn::isbn).reduce((a, b) -> a + ", " + b).orElse("")));
        tablaEdiciones.getColumns().addAll(List.of(ek, ew, ei)); tablaEdiciones.setPlaceholder(new Label("No hay ediciones cargadas."));
        autorData.setPrefRowCount(12); obraData.setPrefRowCount(12); edicionData.setPrefRowCount(8);
    }

    private <T> TableColumn<T, String> columna(String titulo, java.util.function.Function<T, String> valor) {
        TableColumn<T, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(c -> new SimpleStringProperty(valor.apply(c.getValue())));
        return columna;
    }

    private void cargarDatos() {
        try {
            tablaAutores.setItems(FXCollections.observableArrayList(autores.obtenerTodos()));
            tablaObras.setItems(FXCollections.observableArrayList(obras.obtenerTodos()));
            tablaEdiciones.setItems(FXCollections.observableArrayList(libros.obtenerTodos()));
            estado.setText("Base conectada. " + tablaAutores.getItems().size() + " autores, "
                    + tablaObras.getItems().size() + " obras y " + tablaEdiciones.getItems().size() + " ediciones.");
        } catch (SQLException e) { estado.setText("Error: " + e.getMessage()); }
    }

    private void ejecutar(SqlAction accion) {
        try { accion.run(); cargarDatos(); }
        catch (SQLException | IllegalArgumentException e) { mostrarError(e.getMessage()); }
    }

    private void abrirBase(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar base SQLite descargada");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite (*.db, *.sqlite)", "*.db", "*.sqlite"));
        FileChooser.ExtensionFilter todos = new FileChooser.ExtensionFilter("Todos los archivos", "*.*");
        chooser.getExtensionFilters().add(todos);
        var archivo = chooser.showOpenDialog(stage);
        Path seleccion = archivo == null ? null : archivo.toPath();
        if (seleccion != null) { Conexion.establecerRuta(seleccion); cargarDatos(); }
    }

    private void descargarBase(Stage stage) {
        TextInputDialog dialog = new TextInputDialog("https://");
        dialog.setTitle("Descargar base de datos");
        dialog.setHeaderText("Introduce la URL de una base SQLite compatible");
        dialog.setContentText("URL:");
        dialog.showAndWait().ifPresent(url -> {
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName("openlibrary.db");
            var archivo = chooser.showSaveDialog(stage);
            Path destino = archivo == null ? null : archivo.toPath();
            if (destino == null) return;
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
                HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofFile(destino));
                Conexion.establecerRuta(destino); cargarDatos();
            } catch (IOException | InterruptedException | IllegalArgumentException e) { mostrarError(e.getMessage()); }
        });
    }

    private void mostrarError(String mensaje) {
        new Alert(Alert.AlertType.ERROR, mensaje == null ? "Operación no realizada" : mensaje, ButtonType.OK).showAndWait();
    }

    @FunctionalInterface private interface SqlAction { void run() throws SQLException; }
}

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        Application.launch(Aplicacion.class, args);
    }

    public static final class Aplicacion extends AplicacionBase {
        public Aplicacion() {}
    }
}
