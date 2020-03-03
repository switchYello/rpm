CREATE TABLE IF NOT EXISTS `server_info` (
  `id`          INT         NOT NULL AUTO_INCREMENT,
  `server_host` VARCHAR(50) NOT NULL,
  `server_port` INT         NOT NULL,
  `auto_token`  VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id`)
);


CREATE TABLE IF NOT EXISTS `server_worker` (
  `id`          INT         NOT NULL AUTO_INCREMENT,
  `server_port` INT         NOT NULL,
  `local_host`  VARCHAR(50) NOT NULL,
  `local_port`  INT         NOT NULL,
  PRIMARY KEY (`id`)
);