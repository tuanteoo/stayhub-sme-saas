ALTER TABLE properties ADD COLUMN search_text TEXT;

UPDATE properties
SET search_text = LOWER(REPLACE(name || province || district || ward, ' ', ''))
WHERE search_text IS NULL;