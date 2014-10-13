
-- Populate with the defaults of the website

# --- !Ups

INSERT INTO scales("name", "values") VALUES
('Major', '1,3,5,6,8,10,12'),
('Harmonic Minor', '1,3,4,6,8,9,12'),
('Melodic Minor (Ascending)', '1,3,4,6,8,10,12'),
('Melodic Minor (Descending)', '1,3,4,6,8,9,11'),
('Chromatic', '1,2,3,4,5,6,7,8,9,10,11,12'),
('Whole Tone', '1,3,5,7,9,11'),
('Pentatonic Major', '1,3,5,8,10'),
('Pentatonic Minor', '1,4,6,8,11'),
('Pentatonic Blues', '1,4,6,7,8,11'),
('Pentatonic Neutral', '1,3,6,8,11'),
('Octatonic (H-W)', '1,2,4,5,7,8,10,11'),
('Octatonic (W-H)', '1,3,4,6,7,9,10,12'),
('Ionian', '1,3,5,6,8,10,12'),
('Dorian', '1,3,4,6,8,10,11'),
('Phrygian', '1,2,4,6,8,9,11'),
('Lydian', '1,3,5,7,8,10,12'),
('Lydian Augmented', '1,3,5,7,9,10,12'),
('Lydian Minor', '1,3,5,7,8,9,11'),
('Lydian Diminished', '1,3,4,7,8,10,12'),
('Mixolydian', '1,3,5,6,8,10,11'),
('Aeolian', '1,3,4,6,8,9,11'),
('Locrian', '1,2,4,6,7,9,11'),
('Bebop Major', '1,3,5,6,8,9,10,12'),
('Bebop Minor', '1,3,4,5,6,8,10,11'),
('Bebop Dominant', '1,3,5,6,8,10,11,12'),
('Bebop Half Diminished', '1,2,4,6,7,8,9,12'),
('Blues Variation 1', '1,4,6,7,8,11,12'),
('Blues Variation 2', '1,4,5,6,7,8,11,12'),
('Blues Variation 3', '1,4,5,6,7,8,10,11,12'),
('Mixo-Blues', '1,4,5,6,7,8,11'),
('Major Blues Scale', '1,3,4,5,8,10'),
('Dominant Pentatonic', '1,3,5,8,11'),
('Chinese 2', '1,3,6,8,10'),
('Hirajoshi 2', '1,5,6,10,12'),
('Iwato', '1,2,6,7,11'),
('Japanese (in sen)', '1,2,6,8,11'),
('Kumoi 2', '1,2,6,8,9'),
('Pelog 2', '1,2,4,8,11'),
('Locrian 6', '1,2,4,6,7,10,11'),
('Ionian #5', '1,3,5,6,8,10,12'),
('Dorian #4', '1,3,4,6,8,10,11'),
('Phrygian Major', '1,2,5,6,8,9,11'),
('Lydian #2', '1,3,5,7,8,10,12'),
('Ultralocrian', '1,2,4,5,7,9,10'),
('Moorish Phrygian', '1,2,4,5,6,8,9,11,12'),
('Algerian', '1,3,4,6,7,8,9,12'),
('Altered', '1,2,4,5,7,9,11'),
('Arabian (a)', '1,3,4,6,7,9,10,12'),
('Arabian (b)', '1,3,5,6,7,9,11'),
('Augmented', '1,4,5,7,9,12'),
('Auxiliary Diminished', '1,3,4,6,7,9,10,12'),
('Auxiliary Augmented', '1,3,5,7,9,11'),
('Auxiliary Diminished Blues', '1,2,4,5,7,8,10,11'),
('Balinese', '1,2,4,8,9'),
('Blues', '1,4,6,7,8,11'),
('Byzantine', '1,2,5,6,8,9,12'),
('Chinese', '1,5,7,8,12'),
('Chinese Mongolian', '1,3,5,8,10'),
('Diatonic', '1,3,5,8,10'),
('Diminished', '1,3,4,6,7,9,10,12'),
('Diminished, Half', '1,2,4,5,7,8,10,11'),
('Diminished Whole Tone', '1,2,4,5,7,9,11'),
('Dominant 7th', '1,3,5,6,8,10,11'),
('Double Harmonic', '1,2,5,6,8,9,12'),
('Egyptian', '1,3,6,8,11'),
('Eight Tone Spanish', '1,2,4,5,6,7,9,11'),
('Enigmatic', '1,2,5,7,9,11,12'),
('Ethiopian (A raray)', '1,3,5,6,8,10,12'),
('Half Diminished (Locrian)', '1,2,4,6,7,9,11'),
('Hawaiian', '1,3,4,6,8,10,12'),
('Hindu', '1,3,5,6,8,9,11'),
('Hindustan', '1,3,5,6,8,9,11'),
('Hirajoshi', '1,3,4,8,9'),
('Hungarian Major', '1,4,5,7,8,10,11'),
('Hungarian Gypsy', '1,3,4,7,8,9,12'),
('Hungarian Gypsy Persian', '1,2,5,6,8,9,12'),
('Hungarian Minor', '1,3,4,7,8,9,12'),
('Japanese (A)', '1,2,6,8,9'),
('Japanese (B)', '1,3,6,8,9'),
('Japanese (Ichikosucho)', '1,3,5,6,7,8,10,12'),
('Japanese (Taishikicho)', '1,3,5,6,7,8,10,11,12'),
('Javaneese', '1,2,4,6,8,10,11'),
('Jewish (Adonai Malakh)', '1,2,3,4,6,8,10,11'),
('Jewish (Ahaba Rabba)', '1,2,5,6,8,9,11'),
('Jewish (Magen Abot)', '1,2,4,5,7,9,11,12'),
('Kumoi', '1,3,4,8,10'),
('Leading Whole Tone', '1,3,5,7,9,11,12'),
('Major Locrian', '1,3,5,6,7,9,11'),
('Mohammedan', '1,3,4,6,8,9,12'),
('Natural (Pure) Minor', '1,3,4,6,8,9,11'),
('Neopolitan', '1,2,4,6,8,9,12'),
('Neoploitan Major', '1,2,4,6,8,10,12'),
('Neopolitan Minor', '1,2,4,6,8,9,11'),
('Nine Tone Scale', '1,3,4,5,7,8,9,10,12'),
('Oriental (a)', '1,2,5,6,7,9,11'),
('Oriental (b)', '1,2,5,6,7,10,11'),
('Overtone', '1,3,5,7,8,10,11'),
('Overtone Dominant', '1,3,5,7,8,10,11'),
('Pelog', '1,2,4,8,9'),
('Persian', '1,2,5,6,7,9,12'),
('Prometheus', '1,3,5,7,10,11'),
('Prometheus Neopolitan', '1,2,5,7,10,11'),
('Roumanian Minor', '1,3,4,7,8,10,11'),
('Six Tone Symmetrical', '1,2,5,6,9,10'),
('Spanish Gypsy', '1,2,5,6,8,9,11'),
('Super Locrian', '1,2,4,5,7,9,11');

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

INSERT INTO users(username, password, email)
VALUES
('helloworld','$2a$10$RrStaJfOR4tx7cES5WDBjOWB4l4/GQFQgWGoXf1lpJiErytrRE6b6','helloworld@gmail.com');

# --- !Downs

DELETE FROM "scales";

DELETE FROM "tunings";

DELETE FROM "instruments";

