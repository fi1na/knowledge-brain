-- V5__add_full_text_search.sql
-- Adds PostgreSQL full-text search capability to notes table

-- 1. Add tsvector column to store pre-computed search vectors
ALTER TABLE notes ADD COLUMN search_vector tsvector;

-- 2. Backfill existing rows
UPDATE notes SET search_vector =
    setweight(to_tsvector('english', coalesce(title, '')), 'A') ||
    setweight(to_tsvector('english', coalesce(content, '')), 'B');

-- 3. GIN index for fast full-text lookups
CREATE INDEX idx_notes_search_vector ON notes USING GIN (search_vector);

-- 4. Trigger function to auto-update search_vector on INSERT or UPDATE
CREATE OR REPLACE FUNCTION notes_search_vector_update() RETURNS trigger AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('english', coalesce(NEW.title, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(NEW.content, '')), 'B');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_notes_search_vector_update
    BEFORE INSERT OR UPDATE OF title, content ON notes
    FOR EACH ROW
    EXECUTE FUNCTION notes_search_vector_update();
