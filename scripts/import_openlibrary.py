#!/usr/bin/env python3
"""Importa los dumps JSONL/tabulados de Open Library a la base SQLite local."""

import argparse
import gzip
import json
import sqlite3
import sys
from pathlib import Path


def records(path: Path):
    opener = gzip.open if path.suffix == ".gz" else open
    with opener(path, "rt", encoding="utf-8") as source:
        for line_number, line in enumerate(source, 1):
            parts = line.rstrip("\n").split("\t", 4)
            if len(parts) != 5:
                print(f"{path}:{line_number}: línea ignorada")
                continue
            try:
                yield parts[0], parts[1], int(parts[2]), parts[3], json.loads(parts[4])
            except (ValueError, json.JSONDecodeError) as error:
                print(f"{path}:{line_number}: JSON inválido ({error})")


def insert_authors(connection, path: Path):
    sql = """INSERT OR REPLACE INTO authors(type,key,revision,last_modified,data)
             VALUES(?,?,?,?,?)"""
    with connection:
        for kind, key, revision, modified, data in records(path):
            connection.execute(sql, (kind, key, revision, modified, json.dumps(data, ensure_ascii=False)))


def insert_works(connection, path: Path):
    sql = """INSERT OR REPLACE INTO works(type,key,revision,last_modified,data)
             VALUES(?,?,?,?,?)"""
    relation = "INSERT OR IGNORE INTO author_works(author_key,work_key) VALUES(?,?)"
    with connection:
        for kind, key, revision, modified, data in records(path):
            connection.execute(sql, (kind, key, revision, modified, json.dumps(data, ensure_ascii=False)))
            for author in data.get("authors", []):
                author_key = author.get("author", {}).get("key")
                if author_key:
                    connection.execute(relation, (author_key, key))


def insert_editions(connection, path: Path):
    edition_sql = """INSERT OR REPLACE INTO editions(type,key,revision,last_modified,work_key,data)
                     VALUES(?,?,?,?,?,?)"""
    isbn_sql = "INSERT OR IGNORE INTO edition_isbns(edition_key,isbn) VALUES(?,?)"
    with connection:
        for kind, key, revision, modified, data in records(path):
            works = data.get("works", [])
            work_key = works[0].get("key") if works else None
            if not work_key:
                continue
            connection.execute(edition_sql, (
                kind, key, revision, modified, work_key,
                json.dumps(data, ensure_ascii=False),
            ))
            for isbn in data.get("isbn", []):
                if isinstance(isbn, str) and isbn.strip():
                    connection.execute(isbn_sql, (key, isbn.strip()))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--authors", type=Path, required=True, help="ol_dump_authors*.txt.gz")
    parser.add_argument("--works", type=Path, required=True, help="ol_dump_works*.txt.gz")
    parser.add_argument("--editions", type=Path, required=True, help="ol_dump_editions*.txt.gz")
    parser.add_argument("--database", type=Path, default=Path("libros.db"))
    parser.add_argument("--schema", type=Path, default=Path("sql/libros.sql"))
    args = parser.parse_args()

    missing = [path for path in (args.authors, args.works, args.editions) if not path.is_file()]
    if missing:
        print("Faltan archivos de Open Library:", file=sys.stderr)
        for path in missing:
            print(f"  - {path}", file=sys.stderr)
        print(
            "\nDescárgalos antes de importar. Consulta README.md o ejecuta:\n"
            "  mkdir -p datos-openlibrary\n"
            "  curl -L https://openlibrary.org/data/ol_dump_authors_latest.txt.gz "
            "-o datos-openlibrary/authors.txt.gz\n"
            "  curl -L https://openlibrary.org/data/ol_dump_works_latest.txt.gz "
            "-o datos-openlibrary/works.txt.gz\n"
            "  curl -L https://openlibrary.org/data/ol_dump_editions_latest.txt.gz "
            "-o datos-openlibrary/editions.txt.gz",
            file=sys.stderr,
        )
        return 2

    args.database.parent.mkdir(parents=True, exist_ok=True)
    connection = sqlite3.connect(args.database)
    connection.execute("PRAGMA foreign_keys = ON")
    connection.executescript(args.schema.read_text(encoding="utf-8"))
    connection.execute("PRAGMA journal_mode = WAL")
    try:
        print("Importando autores...")
        insert_authors(connection, args.authors)
        print("Importando obras y relaciones...")
        insert_works(connection, args.works)
        print("Importando ediciones e ISBN...")
        insert_editions(connection, args.editions)
    finally:
        connection.execute("PRAGMA wal_checkpoint(TRUNCATE)")
        connection.close()
    print(f"Base creada: {args.database}")


if __name__ == "__main__":
    main()
