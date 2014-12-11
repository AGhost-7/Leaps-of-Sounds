
# --- !Ups

-- The begin_session function will handle what the trigger
-- is supposed to do, so we can drop it from the database.

DROP TRIGGER IF EXISTS tokens_clean_on_insert ON tokens;

DROP FUNCTION IF EXISTS tokens_cleaner();

CREATE FUNCTION begin_session(
	user_ref INT,
	new_token CHAR(37)
)
	RETURNS VOID AS
$$

BEGIN

	INSERT INTO tokens(token, user_id) 
	VALUES (new_token, user_ref);;
	
	UPDATE "users"
	SET last_login = CURRENT_TIMESTAMP
	WHERE id = user_ref;;
	
	-- This way I can remove the trigger.
	DELETE FROM tokens 
	WHERE user_id = user_ref 
		AND created_on < CURRENT_TIMESTAMP - INTERVAL '2 weeks';;
	
END;;
$$ LANGUAGE plpgsql;

# --- !Downs

DROP FUNCTION IF EXISTS begin_session(
	user_id INT,
	token CHAR(37)
);

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