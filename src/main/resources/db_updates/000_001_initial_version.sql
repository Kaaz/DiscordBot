DROP TABLE IF EXISTS `banks`;

CREATE TABLE IF NOT EXISTS `banks` (
  `id`              INT(11)   NOT NULL AUTO_INCREMENT,
  `user`            INT(11)   NOT NULL,
  `current_balance` INT(11)            DEFAULT NULL,
  `created_on`      TIMESTAMP NULL     DEFAULT NULL,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  AUTO_INCREMENT = 1;

DROP TABLE IF EXISTS `bank_reserverations`;

CREATE TABLE IF NOT EXISTS `bank_reserverations` (
  `id`               INT(11)   NOT NULL,
  `bank`             INT(11)   NOT NULL,
  `amount`           INT(11)   NOT NULL,
  `reason`           VARCHAR(255)   DEFAULT NULL,
  `reservation_time` TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

DROP TABLE IF EXISTS `bank_transactions`;

CREATE TABLE IF NOT EXISTS `bank_transactions` (
  `id`               INT(11)   NOT NULL AUTO_INCREMENT,
  `bank_from`        INT(11)   NOT NULL,
  `bank_to`          INT(11)   NOT NULL,
  `transaction_date` TIMESTAMP NULL     DEFAULT NULL,
  `description`      VARCHAR(255)       DEFAULT NULL,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  AUTO_INCREMENT = 1;

DROP TABLE IF EXISTS `channels`;
CREATE TABLE IF NOT EXISTS `channels` (
  `id`         INT(11) NOT NULL AUTO_INCREMENT,
  `discord_id` VARCHAR(255)     DEFAULT NULL,
  `server_id`  INT(11)          DEFAULT NULL,
  `name`       VARCHAR(255)     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `channels_discord_id_uindex` (`discord_id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  AUTO_INCREMENT = 1;

DROP TABLE IF EXISTS `commands`;

CREATE TABLE IF NOT EXISTS `commands` (
  `id`     INT(11)     NOT NULL AUTO_INCREMENT,
  `server` INT(11)              DEFAULT NULL,
  `input`  VARCHAR(50) NOT NULL,
  `output` TEXT        NOT NULL,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  AUTO_INCREMENT = 1;

DROP TABLE IF EXISTS `command_cooldown`;

CREATE TABLE IF NOT EXISTS `command_cooldown` (
  `command`     VARCHAR(64) NOT NULL,
  `target_id`   VARCHAR(64) NOT NULL DEFAULT '',
  `target_type` INT(11)     NOT NULL DEFAULT '0',
  `last_time`   INT(21)     NOT NULL,
  PRIMARY KEY (`command`, `target_id`, `target_type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

DROP TABLE IF EXISTS `command_log`;

CREATE TABLE IF NOT EXISTS `command_log` (
  `id`           INT(11)     NOT NULL AUTO_INCREMENT,
  `user_id`      INT(11)     NOT NULL,
  `guild`        INT(11)              DEFAULT NULL,
  `command`      VARCHAR(64) NOT NULL,
  `args`         TEXT,
  `execute_date` TIMESTAMP   NULL     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  AUTO_INCREMENT = 1;

DROP TABLE IF EXISTS `guild_member`;

CREATE TABLE IF NOT EXISTS `guild_member` (
  `guild_id`  INT(11)   NOT NULL,
  `user_id`   INT(11)   NOT NULL,
  `join_date` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`guild_id`, `user_id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

DROP TABLE IF EXISTS `guild_settings`;

CREATE TABLE IF NOT EXISTS `guild_settings` (
  `guild`  INT(11)      NOT NULL,
  `name`   VARCHAR(255) NOT NULL,
  `config` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`guild`, `name`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

DROP TABLE IF EXISTS playlist;
DROP TABLE IF EXISTS music;

CREATE TABLE IF NOT EXISTS `playlist` (
  `id`            INT(11)     NOT NULL AUTO_INCREMENT,
  `youtubecode`   VARCHAR(32) NOT NULL,
  `filename`      VARCHAR(64) NOT NULL,
  `title`         VARCHAR(128)         DEFAULT NULL,
  `artist`        VARCHAR(128)         DEFAULT NULL,
  `lastplaydate`  INT(11)     NOT NULL DEFAULT '0',
  `banned`        INT(11)     NOT NULL DEFAULT '0',
  `youtube_title` VARCHAR(255)         DEFAULT NULL,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  AUTO_INCREMENT = 1;

DROP TABLE IF EXISTS `poe_token`;

CREATE TABLE IF NOT EXISTS `poe_token` (
  `user_id`    INT(11) NOT NULL,
  `session_id` VARCHAR(128) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

DROP TABLE IF EXISTS guilds;
DROP TABLE IF EXISTS servers;

CREATE TABLE IF NOT EXISTS `servers` (
  `id`         INT(11)      NOT NULL AUTO_INCREMENT,
  `discord_id` VARCHAR(255) NOT NULL,
  `name`       VARCHAR(128)          DEFAULT NULL,
  `owner`      INT(11)      NOT NULL,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  AUTO_INCREMENT = 1;

DROP TABLE IF EXISTS `services`;

CREATE TABLE IF NOT EXISTS `services` (
  `id`           INT(11)     NOT NULL AUTO_INCREMENT,
  `name`         VARCHAR(32) NOT NULL,
  `display_name` VARCHAR(64) NOT NULL,
  `description`  TEXT,
  `activated`    INT(11)              DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `subscription_service_name_uindex` (`name`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  AUTO_INCREMENT = 1;

DROP TABLE IF EXISTS `service_variables`;

CREATE TABLE IF NOT EXISTS `service_variables` (
  `service_id` INT(11)     NOT NULL,
  `variable`   VARCHAR(64) NOT NULL,
  `value`      VARCHAR(128) DEFAULT NULL,
  PRIMARY KEY (`service_id`, `variable`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

DROP TABLE IF EXISTS `subscriptions`;

CREATE TABLE IF NOT EXISTS `subscriptions` (
  `server_id`  INT(11) NOT NULL,
  `channel_id` INT(11) NOT NULL,
  `service_id` INT(11) NOT NULL DEFAULT '0',
  `subscribed` INT(11)          DEFAULT NULL,
  PRIMARY KEY (`server_id`, `channel_id`, `service_id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

DROP TABLE IF EXISTS `template_texts`;

CREATE TABLE IF NOT EXISTS `template_texts` (
  `id`        INT(11)      NOT NULL AUTO_INCREMENT,
  `keyphrase` VARCHAR(50)  NOT NULL,
  `text`      VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `txt_search` (`keyphrase`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  AUTO_INCREMENT = 1;

DROP TABLE IF EXISTS `users`;

CREATE TABLE IF NOT EXISTS `users` (
  `id`         INT(11)     NOT NULL AUTO_INCREMENT,
  `discord_id` VARCHAR(64) NOT NULL,
  `name`       VARCHAR(128)         DEFAULT NULL,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  AUTO_INCREMENT = 1;

CREATE TABLE bot_meta (
  meta_name  VARCHAR(32) PRIMARY KEY NOT NULL,
  meta_value VARCHAR(32)
);