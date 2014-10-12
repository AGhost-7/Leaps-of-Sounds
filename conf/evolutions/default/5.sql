
# --- !Ups

CREATE TABLE users(
  "id" SERIAL NOT NULL,
  "username" VarChar(45) NOT NULL UNIQUE,
  "password" VarChar(200) NOT NULL,
  "email" VarChar(50) NOT NULL UNIQUE, 
  PRIMARY KEY("id")
);


# --- !Downs

DROP TABLE users;
