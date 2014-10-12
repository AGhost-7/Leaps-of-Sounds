 -- Create tuning and instruments tables.
 
# --- !Ups

CREATE TABLE "instruments" (
  "id" SERIAL NOT NULL,
  "name" VARCHAR(45) NOT NULL,
  "strings" INT NOT NULL,
  PRIMARY KEY ("id")
);

CREATE TABLE "tunings" (
  "id" SERIAL NOT NULL,
  "name" VARCHAR(45) NOT NULL,
  "values" VARCHAR(45) NOT NULL,
  "instrument" INT NOT NULL 
    REFERENCES "instruments"("id"),
  PRIMARY KEY ("id")
);


# --- !Downs

DROP TABLE "instruments";

DROP TABLE "tunings";