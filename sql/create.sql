SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

--
-- Database: `discordbot`
--

DELIMITER $$
--
-- Functions
--
DROP FUNCTION IF EXISTS `levenshtein`$$
CREATE DEFINER =`root`@`localhost` FUNCTION `levenshtein`(s1 VARCHAR(255), s2 VARCHAR(255))
  RETURNS INT(11)
DETERMINISTIC
  BEGIN
    DECLARE s1_len, s2_len, i, j, c, c_temp, cost INT;
    DECLARE s1_char CHAR;

    DECLARE cv0, cv1 VARBINARY(256);
    SET s1_len = CHAR_LENGTH(s1), s2_len = CHAR_LENGTH(s2), cv1 = 0x00, j = 1, i = 1, c = 0;
    IF s1 = s2
    THEN
      RETURN 0;
    ELSEIF s1_len = 0
      THEN
        RETURN s2_len;
    ELSEIF s2_len = 0
      THEN
        RETURN s1_len;
    ELSE
      WHILE j <= s2_len DO
        SET cv1 = CONCAT(cv1, UNHEX(HEX(j))), j = j + 1;
      END WHILE;
      WHILE i <= s1_len DO
        SET s1_char = SUBSTRING(s1, i, 1), c = i, cv0 = UNHEX(HEX(i)), j = 1;
        WHILE j <= s2_len DO
          SET c = c + 1;
          IF s1_char = SUBSTRING(s2, j, 1)
          THEN
            SET cost = 0;
          ELSE SET cost = 1;
          END IF;
          SET c_temp = CONV(HEX(SUBSTRING(cv1, j, 1)), 16, 10) + cost;
          IF c > c_temp
          THEN SET c = c_temp; END IF;
          SET c_temp = CONV(HEX(SUBSTRING(cv1, j + 1, 1)), 16, 10) + 1;
          IF c > c_temp
          THEN
            SET c = c_temp;
          END IF;
          SET cv0 = CONCAT(cv0, UNHEX(HEX(c))), j = j + 1;
        END WHILE;
        SET cv1 = cv0, i = i + 1;
      END WHILE;
    END IF;
    RETURN c;
  END$$

DROP FUNCTION IF EXISTS `levenshtein_ratio`$$
CREATE DEFINER =`root`@`localhost` FUNCTION `levenshtein_ratio`(s1 VARCHAR(255), s2 VARCHAR(255))
  RETURNS INT(11)
DETERMINISTIC
  BEGIN
    DECLARE s1_len, s2_len, max_len INT;
    SET s1_len = LENGTH(s1), s2_len = LENGTH(s2);
    IF s1_len > s2_len
    THEN
      SET max_len = s1_len;
    ELSE
      SET max_len = s2_len;
    END IF;
    RETURN ROUND((1 - LEVENSHTEIN(s1, s2) / max_len) * 100);
  END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `banks`
--

DROP TABLE IF EXISTS `banks`;
CREATE TABLE IF NOT EXISTS `banks` (
  `id`              INT(11)   NOT NULL AUTO_INCREMENT,
  `user`            INT(11)   NOT NULL,
  `current_balance` INT(11)            DEFAULT NULL,
  `created_on`      TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  AUTO_INCREMENT = 1;

-- --------------------------------------------------------

--
-- Table structure for table `bank_reserverations`
--

DROP TABLE IF EXISTS `bank_reserverations`;
CREATE TABLE IF NOT EXISTS `bank_reserverations` (
  `id`               INT(11)   NOT NULL,
  `bank`             INT(11)   NOT NULL,
  `amount`           INT(11)   NOT NULL,
  `reason`           VARCHAR(255)       DEFAULT NULL,
  `reservation_time` TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

-- --------------------------------------------------------

--
-- Table structure for table `bank_transactions`
--

DROP TABLE IF EXISTS `bank_transactions`;
CREATE TABLE IF NOT EXISTS `bank_transactions` (
  `id`               INT(11)   NOT NULL AUTO_INCREMENT,
  `bank_from`        INT(11)   NOT NULL,
  `bank_to`          INT(11)   NOT NULL,
  `transaction_date` TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
  `description`      VARCHAR(255)       DEFAULT NULL,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  AUTO_INCREMENT = 1;

-- --------------------------------------------------------

--
-- Table structure for table `channels`
--

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

-- --------------------------------------------------------

--
-- Table structure for table `commands`
--

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

-- --------------------------------------------------------

--
-- Table structure for table `command_cooldown`
--

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

-- --------------------------------------------------------

--
-- Table structure for table `command_log`
--

DROP TABLE IF EXISTS `command_log`;
CREATE TABLE IF NOT EXISTS `command_log` (
  `id`           INT(11)     NOT NULL AUTO_INCREMENT,
  `user_id`      INT(11)     NOT NULL,
  `guild`        INT(11)              DEFAULT NULL,
  `command`      VARCHAR(64) NOT NULL,
  `args`         TEXT,
  `execute_date` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  AUTO_INCREMENT = 1;

-- --------------------------------------------------------

--
-- Table structure for table `guild_member`
--

DROP TABLE IF EXISTS `guild_member`;
CREATE TABLE IF NOT EXISTS `guild_member` (
  `guild_id`  INT(11)   NOT NULL,
  `user_id`   INT(11)   NOT NULL,
  `join_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`guild_id`, `user_id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

-- --------------------------------------------------------

--
-- Table structure for table `guild_settings`
--

DROP TABLE IF EXISTS `guild_settings`;
CREATE TABLE IF NOT EXISTS `guild_settings` (
  `guild`  INT(11)      NOT NULL,
  `name`   VARCHAR(255) NOT NULL,
  `config` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`guild`, `name`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

-- --------------------------------------------------------

--
-- Table structure for table `playlist`
--

DROP TABLE IF EXISTS `playlist`;
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

-- --------------------------------------------------------

--
-- Table structure for table `poe_token`
--

DROP TABLE IF EXISTS `poe_token`;
CREATE TABLE IF NOT EXISTS `poe_token` (
  `user_id`    INT(11) NOT NULL,
  `session_id` VARCHAR(128) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

-- --------------------------------------------------------

--
-- Table structure for table `servers`
--

DROP TABLE IF EXISTS `servers`;
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

-- --------------------------------------------------------

--
-- Table structure for table `services`
--

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

-- --------------------------------------------------------

--
-- Table structure for table `service_variables`
--

DROP TABLE IF EXISTS `service_variables`;
CREATE TABLE IF NOT EXISTS `service_variables` (
  `service_id` INT(11)     NOT NULL,
  `variable`   VARCHAR(64) NOT NULL,
  `value`      VARCHAR(128) DEFAULT NULL,
  PRIMARY KEY (`service_id`, `variable`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

-- --------------------------------------------------------

--
-- Table structure for table `subscriptions`
--

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

-- --------------------------------------------------------

--
-- Table structure for table `template_texts`
--

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

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

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