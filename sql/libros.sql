-- Esquema compatible con LibrariesHacked/openlibrary-search.
-- El repositorio original importa los dumps oficiales en PostgreSQL.
CREATE TABLE IF NOT EXISTS authors (
    type TEXT,
    key TEXT PRIMARY KEY,
    revision INTEGER,
    last_modified TEXT,
    data TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS works (
    type TEXT,
    key TEXT PRIMARY KEY,
    revision INTEGER,
    last_modified TEXT,
    data TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS author_works (
    author_key TEXT NOT NULL,
    work_key TEXT NOT NULL,
    PRIMARY KEY (author_key, work_key),
    FOREIGN KEY (author_key) REFERENCES authors(key),
    FOREIGN KEY (work_key) REFERENCES works(key)
);

CREATE TABLE IF NOT EXISTS editions (
    type TEXT,
    key TEXT PRIMARY KEY,
    revision INTEGER,
    last_modified TEXT,
    work_key TEXT NOT NULL,
    data TEXT NOT NULL,
    FOREIGN KEY (work_key) REFERENCES works(key)
);

CREATE TABLE IF NOT EXISTS edition_isbns (
    edition_key TEXT NOT NULL,
    isbn TEXT NOT NULL,
    PRIMARY KEY (edition_key, isbn),
    UNIQUE (isbn),
    FOREIGN KEY (edition_key) REFERENCES editions(key) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_edition_isbns_isbn ON edition_isbns(isbn);
CREATE INDEX IF NOT EXISTS idx_works_title ON works(key);
CREATE INDEX IF NOT EXISTS idx_editions_work ON editions(work_key);
