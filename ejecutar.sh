#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
PROJECT="$ROOT/LibreriaDB"

if command -v mvn >/dev/null 2>&1; then
    cd "$PROJECT"
    exec mvn javafx:run -Dlibrarium.db="$ROOT/libros.db" "$@"
fi

JAVA_HOME="${JAVA_HOME:-}"
if [[ -z "$JAVA_HOME" ]]; then
    JAVA_BIN="$(readlink -f "$(command -v java)")"
    JAVA_HOME="$(dirname "$(dirname "$JAVA_BIN")")"
fi
JAVAC="$JAVA_HOME/bin/javac"
JAVA="$JAVA_HOME/bin/java"
M2="${HOME}/.m2/repository"
FX_VERSION="21.0.5"
SQLITE_VERSION="3.46.1.0"
SLF4J_VERSION="1.7.36"
FX_BASE="$M2/org/openjfx/javafx-base/$FX_VERSION/javafx-base-$FX_VERSION-linux.jar"
FX_GRAPHICS="$M2/org/openjfx/javafx-graphics/$FX_VERSION/javafx-graphics-$FX_VERSION-linux.jar"
FX_CONTROLS="$M2/org/openjfx/javafx-controls/$FX_VERSION/javafx-controls-$FX_VERSION-linux.jar"
SQLITE="$M2/org/xerial/sqlite-jdbc/$SQLITE_VERSION/sqlite-jdbc-$SQLITE_VERSION.jar"
SLF4J="$M2/org/slf4j/slf4j-api/$SLF4J_VERSION/slf4j-api-$SLF4J_VERSION.jar"

for file in "$JAVAC" "$JAVA" "$FX_BASE" "$FX_GRAPHICS" "$FX_CONTROLS" "$SQLITE" "$SLF4J"; do
    if [[ ! -f "$file" || ! -x "$file" && "$file" == "$JAVAC" || ! -x "$file" && "$file" == "$JAVA" ]]; then
        echo "Falta un requisito para ejecutar LIBRARIUM: $file" >&2
        echo "Instala Maven o ejecuta el proyecto desde IntelliJ IDEA." >&2
        exit 1
    fi
done

CLASSES="$PROJECT/target/classes"
rm -rf "$CLASSES"
mkdir -p "$CLASSES"
SOURCE_LIST="$(mktemp)"
trap 'rm -f "$SOURCE_LIST"' EXIT
find "$PROJECT/src/main/java" -name '*.java' -print | sort > "$SOURCE_LIST"

"$JAVAC" --release 17 \
    --module-path "$FX_BASE:$FX_GRAPHICS:$FX_CONTROLS" \
    --add-modules javafx.controls \
    -cp "$SQLITE" \
    -d "$CLASSES" \
    @"$SOURCE_LIST"

cd "$PROJECT"
exec "$JAVA" --module-path "$FX_BASE:$FX_GRAPHICS:$FX_CONTROLS" \
    --add-modules javafx.controls \
    --enable-native-access=ALL-UNNAMED \
    -Dlibrarium.db="$ROOT/libros.db" \
    -cp "$CLASSES:$SQLITE:$SLF4J" \
    org.example.Main "$@"
