
-- Create tables with proper references and stuff. ~AGhost

# --- !Ups

-- End user entity, for the most part this table isn't going to be used as 
-- much due to tokens table, which contains the id anyway.

CREATE TABLE users(
	"id" SERIAL NOT NULL,
	"username" VarChar(45) NOT NULL UNIQUE,
	"password" VarChar(200) NOT NULL,
	"email" VarChar(50) NOT NULL UNIQUE, 
	PRIMARY KEY("id")
);

-- Contains stock scales and user-defined scales. If user_id is null then no
-- one should be able to delete the row. Enforced by server instead of DB.

CREATE TABLE "scales" (
  "id" SERIAL NOT NULL,
  "name" VARCHAR(65) NULL,
  "values" VARCHAR(65) NULL,
  "user_id" INTEGER 
		REFERENCES "users"("id"),
  PRIMARY KEY ("id")
);

-- user_id column has same purpose as above.

CREATE TABLE "instruments" (
	"id" SERIAL NOT NULL,
	"name" VARCHAR(45) NOT NULL,
	"strings" INT NOT NULL,
	"user_id" INTEGER REFERENCES "users"("id"),
	PRIMARY KEY ("id")
);

-- Yep.

CREATE TABLE "tunings" (
	"id" SERIAL NOT NULL,
	"name" VARCHAR(45) NOT NULL,
	"values" VARCHAR(45) NOT NULL,
	"instrument" INT NOT NULL 
		REFERENCES "instruments"("id"),
	"user_id" INTEGER 
		REFERENCES "users"("id"),
	PRIMARY KEY ("id")
);

-- Table is used to store session UUID tokens. This way, even the hashed 
-- password isn't passed around, and each new session generates a new token.

CREATE TABLE tokens (
	token CHAR(37) NOT NULL,
	user_id INTEGER NOT NULL,
	created_on TIMESTAMP NOT NULL DEFAULT NOW(),
	PRIMARY KEY(token)
);

-- Following trigger is used to keep the tokens table clean and ensure that 
-- they expire after 2 weeks for security reasons.

-- n.b. Double semi-colons for escaping.

CREATE FUNCTION tokens_cleaner() RETURNS TRIGGER
	LANGUAGE plpgsql
	AS $$
BEGIN
	DELETE FROM tokens WHERE created_on < NOW() - INTERVAL '2 weeks';;
	RETURN NEW;;
END;;
$$;

CREATE TRIGGER tokens_clean_on_insert
	AFTER INSERT ON tokens
	EXECUTE PROCEDURE tokens_cleaner();

# --- !Downs

DROP TABLE scales;

DROP TABLE "tunings";

DROP TABLE "instruments";

DROP TRIGGER tokens_clean_on_insert ON "tokens";

DROP FUNCTION tokens_cleaner();

DROP TABLE tokens;

DROP TABLE users;
