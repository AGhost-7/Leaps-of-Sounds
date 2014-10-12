# --- !Ups

CREATE TABLE "scales" (
  "id" SERIAL NOT NULL,
  "name" VARCHAR(65) NULL,
  "values" VARCHAR(65) NULL,
  PRIMARY KEY ("id")
);

# --- !Downs

DROP TABLE scales;