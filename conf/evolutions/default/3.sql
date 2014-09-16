 -- Create tuning and instruments tables.
 
# --- !Ups

CREATE TABLE `instruments` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `strings` INT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `instruments_name_UNIQUE` (`name` ASC));

CREATE TABLE `tunings` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `values` VARCHAR(45) NOT NULL,
  `instrument` INT NULL,
  PRIMARY KEY (`id`),
  INDEX `id_idx` (`instrument` ASC),
  CONSTRAINT `id`
    FOREIGN KEY (`instrument`)
    REFERENCES `instruments` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


# --- !Downs

DROP TABLE `instruments`;

DROP TABLE `tunings`;