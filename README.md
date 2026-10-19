# LIBRARIUM

Aplicación JavaFX escrita en Java 17 para consultar un catálogo basado en
el modelo de datos de [Open Library](https://openlibrary.org/). El proyecto
usa Maven, JavaFX, JDBC y SQLite. No requiere Python, MySQL ni un servidor
externo para arrancar.

## Estructura

```text
.
├── LibreriaDB/
│   ├── pom.xml
│   └── src/main/java/org/example/
│       ├── Main.java              # Ventana JavaFX
│       ├── conexion/Conexion.java
│       ├── modelo/
│       │   ├── Autor.java
│       │   ├── Libro.java
│       │   ├── LibroIsbn.java
│       │   └── Obra.java
│       ├── repository/
│       │   ├── AutorDAO.java
│       │   ├── AutorDAOJdbc.java
│       │   ├── LibroDAO.java
│       │   ├── LibroDAOJdbc.java
│       │   ├── ObraDAO.java
│       │   └── ObraDAOJdbc.java
│       └── service/
│           ├── AutorServicio.java
│           └── LibroServicio.java
├── sql/libros.sql
├── pom.xml
└── README.md
```

El proyecto Maven está ubicado en `LibreriaDB` y la aplicación ya no contiene
la lógica de departamentos ni empleados.

## Modelo de datos

`sql/libros.sql` es la fuente del esquema SQLite y define cinco tablas:

| Tabla | Uso |
| --- | --- |
| `authors` | Autores y su documento JSON original |
| `works` | Obras bibliográficas |
| `author_works` | Relación muchos a muchos entre autores y obras |
| `editions` | Ediciones concretas vinculadas a una obra |
| `edition_isbns` | ISBN asociados a cada edición |

Las claves conservan el formato canónico de Open Library, por ejemplo
`/authors/OL...`, `/works/OL...` y `/books/OL...`. El JSON completo se guarda
en `data` para conservar atributos que no forman columnas independientes.
El ISBN identifica una edición, no la obra: una obra puede tener varias
ediciones y una edición puede mantener sus ISBN asociados.

El repositorio externo `LibrariesHacked/openlibrary-search` trabaja con
PostgreSQL y dumps oficiales muy grandes. Este proyecto utiliza una traducción
local a SQLite para que el código sea ejecutable sin instalar PostgreSQL. El
repositorio externo no incluye esos dumps; por tanto, el catálogo local recién
creado está vacío hasta cargar datos.

## Requisitos

- JDK 17 o posterior (el bytecode se fija a Java 17).
- Maven 3.8 o posterior.
- `sqlite3` solo es necesario si se quiere ejecutar el SQL manualmente.

### Configurar el JDK en IntelliJ IDEA

El proyecto necesita un JDK real, no solo un JRE. En IntelliJ abre
**File → Project Structure → SDKs**, pulsa **+ → Add JDK** y selecciona la
carpeta de tu instalación de Java 17 o posterior. Después selecciona ese SDK en
**Project → SDK** y en **Settings → Build Tools → Maven → JDK for importer**.
Finalmente pulsa **Reload All Maven Projects** y ejecuta **Build → Rebuild
Project**.

Si tu instalación tiene otro nombre o ruta, utiliza esa instalación de JDK.
IntelliJ debe usar el mismo JDK para compilar y ejecutar; no mezcles un
`javac` moderno con una JVM 17.

## Preparar la base de datos

La aplicación crea las tablas automáticamente al abrir la conexión. También se
pueden crear de forma explícita desde la raíz del proyecto:

```bash
sqlite3 libros.db < sql/libros.sql
```

Si `sqlite3` no está instalado, basta con ejecutar la aplicación: el driver
SQLite incluido por Maven creará `libros.db` y sus tablas automáticamente.
La aplicación no inserta datos de prueba: mientras no se carguen los dumps,
las dos tablas de la interfaz permanecen vacías.

El repositorio incluye `libros.db` como base SQLite inicial vacía. Contiene las
cinco tablas y sus índices, pero ningún autor, obra, edición ni ISBN. Se puede
regenerar en cualquier momento con:

```bash
rm -f libros.db
sqlite3 libros.db < sql/libros.sql
```

Para usar otra ubicación de la base:

```bash
cd LibreriaDB
mvn exec:java -Dexec.mainClass=org.example.Main \
  -Dlibrarium.db=/ruta/completa/catalogo.db
```

El parámetro `librarium.db` también se puede configurar desde IntelliJ como
opción de la máquina virtual.

## Compilar y ejecutar

La forma rápida de arrancar la aplicación desde la raíz es:

```bash
./ejecutar.sh
```

El lanzador usa Maven si está instalado. Si `mvn` no está disponible, compila
automáticamente con el JDK y las dependencias JavaFX/SQLite descargadas en
`~/.m2`, por lo que no es necesario instalar Maven para una ejecución local.
Si tampoco están esas dependencias, ejecuta primero el proyecto desde IntelliJ
con Maven o instala Maven y ejecuta `mvn dependency:go-offline`.

También se puede hacer manualmente:

```bash
mvn clean compile
cd LibreriaDB
mvn javafx:run
```

Se abre una ventana con las pestañas **Obras y ediciones** y **Autores**. Al
iniciar, las tablas aparecen vacías porque no se incluyen datos de ejemplo.
El botón **Actualizar** vuelve a leer la base después de importar datos.
Los errores de conexión o de lectura se muestran en la barra de estado.

## Descargar y abrir una base de datos

La ventana ofrece dos botones:

1. **Abrir base descargada** permite seleccionar un archivo `.db` o `.sqlite`
   que ya esté en el equipo.
2. **Descargar base por URL** descarga un archivo SQLite compatible, lo abre y
   actualiza las tablas automáticamente.

La URL debe devolver directamente un archivo SQLite que contenga las tablas
`authors`, `works`, `author_works`, `editions` y `edition_isbns`. Los dumps
originales de Open Library (`.gz`) no son archivos SQLite y no se pueden abrir
directamente desde este botón; primero hay que transformarlos mediante el
proceso de importación de `LibrariesHacked/openlibrary-search`.

Las fuentes oficiales recomendadas son:

- [Documentación de Open Library Data Dumps](https://openlibrary.org/developers/dumps)
- [Autores](https://openlibrary.org/data/ol_dump_authors_latest.txt.gz)
- [Obras](https://openlibrary.org/data/ol_dump_works_latest.txt.gz)
- [Ediciones](https://openlibrary.org/data/ol_dump_editions_latest.txt.gz)

Esos tres enlaces descargan dumps oficiales comprimidos y grandes; no se deben
pegar directamente en el botón de descarga de la aplicación. Tras convertirlos
al esquema SQLite de `sql/libros.sql`, se abre el archivo resultante con
**Abrir base descargada**.

### Convertir los dumps a SQLite

El proyecto incluye `scripts/import_openlibrary.py`. El importador procesa los
archivos comprimidos línea a línea y guarda los JSON originales en `data`; no
intenta cargar todo el catálogo en memoria. Necesitas Python 3.10 o posterior,
espacio libre suficiente para los dumps descomprimidos y una ejecución larga.

Descarga los tres archivos oficiales:

```bash
mkdir -p datos-openlibrary
curl -L https://openlibrary.org/data/ol_dump_authors_latest.txt.gz \
  -o datos-openlibrary/authors.txt.gz
curl -L https://openlibrary.org/data/ol_dump_works_latest.txt.gz \
  -o datos-openlibrary/works.txt.gz
curl -L https://openlibrary.org/data/ol_dump_editions_latest.txt.gz \
  -o datos-openlibrary/editions.txt.gz
```

Comprueba que existen antes de importar:

```bash
ls -lh datos-openlibrary/*.txt.gz
```

Si aparece `FileNotFoundError` para `datos-openlibrary/authors.txt.gz`,
significa que todavía no se ha descargado ese archivo o que tiene otro nombre.
El importador comprueba los tres ficheros antes de modificar `libros.db` y
muestra los comandos de descarga si falta alguno.

Después importa en el orden autores, obras y ediciones:

```bash
python3 scripts/import_openlibrary.py \
  --authors datos-openlibrary/authors.txt.gz \
  --works datos-openlibrary/works.txt.gz \
  --editions datos-openlibrary/editions.txt.gz \
  --database libros.db
```

Para una prueba pequeña, utiliza una copia recortada de cada dump, manteniendo
los autores referenciados por las obras y las obras referenciadas por las
ediciones. No ejecutes la importación completa sin comprobar antes el espacio
disponible: los dumps oficiales ocupan decenas de gigabytes y el catálogo
completo requiere mucho espacio adicional en SQLite.

No hay actualmente un enlace oficial que descargue una SQLite ya convertida.
Los archivos de terceros pueden estar desactualizados o no tener exactamente
este esquema; antes de abrir uno, comprueba:

```bash
sqlite3 catalogo.db \
  "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;"
```

Debe mostrar `authors`, `works`, `author_works`, `editions` y
`edition_isbns`. Haz siempre una copia de seguridad antes de editar una base
descargada.

## Gestionar los datos

Cada pestaña tiene formulario y operaciones:

- **Autores**: crear, modificar, borrar y limpiar autores.
- **Obras**: crear, modificar, borrar y limpiar obras.
- **Ediciones e ISBN**: crear, modificar, borrar y gestionar ISBN separados
  por comas.

Para modificar un registro se selecciona en la tabla, se editan sus campos y
se pulsa **Guardar**. Las claves Open Library son identificadores únicos. Las
claves foráneas se respetan: no se puede crear una edición si su obra no
existe, ni borrar una obra o autor que todavía tenga relaciones dependientes.

En IntelliJ puedes ejecutar la configuración **LIBRARIUM JavaFX (Maven)**. Esta
configuración ejecuta `javafx:run`, compila antes de arrancar y añade
automáticamente `target/classes` y todas las dependencias al classpath. Si
usas una configuración de tipo **Application**, ejecuta antes **Build →
Build Project** para que exista `LibreriaDB/target/classes`.
Si IntelliJ muestra `package javafx.application does not exist`, pulsa
**Reload All Maven Projects** y comprueba que el módulo `LIBRARIUM` está
seleccionado. El módulo incluye JavaFX 21.0.5 y SQLite JDBC; no hay que añadir
los JAR manualmente desde el editor.
La configuración **LIBRARIUM JavaFX** usa el módulo raíz `LIBRARIUM`, cuyo
output está fijado a `LibreriaDB/target/classes`; no cambies ese módulo por uno
vacío o por el proyecto Maven padre.

También se puede ejecutar directamente después de compilar:

```bash
cd LibreriaDB
mvn javafx:run
```

## Cargar datos de Open Library

El proyecto deja preparado el esquema y los DAO, pero no descarga
automáticamente los dumps oficiales porque ocupan decenas de gigabytes. Para
una importación completa hay que seguir el procedimiento del repositorio
externo y transformar la carga PostgreSQL a las cinco tablas SQLite descritas
arriba. Para una prueba pequeña se pueden insertar manualmente registros
válidos respetando primero las obras y autores, después las relaciones, luego
las ediciones y finalmente sus ISBN.

## Capas del código

- `modelo`: records inmutables que representan autores, ediciones e ISBN.
- `repository`: interfaces DAO y consultas JDBC.
- `service`: validaciones de entrada antes de acceder a la persistencia.
- `conexion`: apertura de SQLite y creación idempotente del esquema.
- `Main`: punto de entrada JavaFX y manejo explícito de errores SQL.

No se incluyen archivos generados ni configuraciones específicas de IntelliJ.
`libros.db` se conserva como plantilla SQLite vacía; la fuente reproducible de
su estructura sigue siendo `sql/libros.sql`.

Si aparece `No suitable driver found for: libros.db`, se está intentando abrir
el archivo desde una configuración JDBC externa con una URL incorrecta. La
aplicación debe ejecutarse con `./ejecutar.sh` o con la configuración JavaFX de
IntelliJ. La URL JDBC correcta es `jdbc:sqlite:/ruta/completa/libros.db`; no es
`libros.db` sin el prefijo `jdbc:sqlite:`.
