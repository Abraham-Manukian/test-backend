ALTER TABLE budget
    ADD COLUMN author_id INTEGER REFERENCES author(id);
