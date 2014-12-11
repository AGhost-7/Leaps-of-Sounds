
-- Lets track our users a bit.

ALTER TABLE "users" 
ADD COLUMN "last_login" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- We need some default tuning for instruments
-- otherwise I think this is going to cause some problems.

ALTER TABLE "instruments"
ADD COLUMN "default_tuning" INT DEFAULT 1;

UPDATE "instruments"
SET default_tuning = tunings.id
FROM tunings
WHERE tunings.name = 'Standard' 
	AND tunings.instrument = instruments.id;

-- Lets add a stored procedure now...

CREATE TYPE id_tuple_two AS (
	id_one INTEGER, 
	id_two INTEGER
);

CREATE FUNCTION insert_instrument(
	inst_name VarChar(45), 
	inst_strings INT, 
	user_id INT, 
	tun_name VarChar(45),
	tun_values VarChar(45)
)
	RETURNS id_tuple_two AS 
$$
	
DECLARE 
	tupl id_tuple_two;
	
BEGIN
	
	INSERT INTO "instruments"(id, "name","strings","user_id")
	VALUES(DEFAULT, inst_name, inst_strings, user_id)
	RETURNING id INTO tupl.id_one;
	
	INSERT INTO "tunings"(id, "name", "values","instrument", "user_id")
	VALUES(DEFAULT, tun_name, tun_values, tupl.id_one, user_id)
	RETURNING id INTO tupl.id_two;
	
	UPDATE "instruments"
	SET "default_tuning" = tupl.id_two
	WHERE id = tupl.id_one;
	
	RETURN tupl;
	
END
$$ LANGUAGE plpgsql;


-- I forgot to add the cascading delete to this one.


ALTER TABLE "tunings"
DROP CONSTRAINT tunings_instrument_fkey,
ADD FOREIGN KEY(instrument)
	REFERENCES instruments(id)
	ON DELETE CASCADE;














