--- Populate tables with some bits of data.

# --- !Ups

INSERT INTO "instruments"("name", "strings")
VALUES
('Guitar',6),
('Bass Guitar', 4),
('Guitar (7 Strings)', 7),
('Ukulele', 4); 

INSERT INTO tunings("name", "values", "instrument") VALUES
('Standard','55,48,52,57',(SELECT id FROM instruments WHERE "name"='Ukulele')),
('Soprano','47,47,54,47', (SELECT id FROM instruments WHERE "name"='Ukulele')),
('Standard','17,22,27,32',(SELECT id FROM instruments WHERE "name"='Bass Guitar')),
('Standard','28,33,38,43,47,52',(SELECT id FROM instruments WHERE "name"='Guitar')),
('Drop D','26,33,38,43,47,52',(SELECT id FROM instruments WHERE "name"='Guitar')),
('D Modal','26,33,38,43,45,50',(SELECT id FROM instruments WHERE "name"='Guitar')),
-- C, Db, D, Eb, E, F, Gb, G, Ab, A, Bb, B
-- B, E, A, D, G, B, e
('Standard','23,28,33,38,43,47,52', (SELECT id FROM instruments WHERE "name"='Guitar (7 Strings)')),
-- C, E, A, D, G, B, e
('Brazilian','24,28,33,38,43,47,52', (SELECT id FROM instruments WHERE "name"='Guitar (7 Strings)'));


# --- !Downs

DELETE FROM "tunings";

DELETE FROM "instruments";

