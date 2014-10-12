# --- !Ups

CREATE TABLE `scales` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(65) NULL,
  `values` VARCHAR(65) NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `scales_name_UNIQUE` (`name` ASC)
);

# --- !Downs

DROP TABLE `scales`;