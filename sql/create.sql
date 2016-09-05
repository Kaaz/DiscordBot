/*
MySQL Data Transfer
Source Host: localhost
Source Database: discord
Target Host: localhost
Target Database: discord
Date: 23-8-2016 11:21:40
*/

SET FOREIGN_KEY_CHECKS = 0;
-- ----------------------------
-- Table structure for commands
-- ----------------------------
DROP TABLE IF EXISTS `commands`;
CREATE TABLE `commands` (
  `id`     INT(11)                      NOT NULL AUTO_INCREMENT,
  `server` INT(11)                      NOT NULL,
  `input`  VARCHAR(50)
           COLLATE utf8_unicode_ci      NOT NULL,
  `output` TEXT COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 13
  DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;

-- ----------------------------
-- Table structure for guild_settings
-- ----------------------------
DROP TABLE IF EXISTS `guild_settings`;
CREATE TABLE `guild_settings` (
  `guild`  INT(11)                 NOT NULL,
  `name`   VARCHAR(255)
           COLLATE utf8_unicode_ci NOT NULL,
  `config` VARCHAR(255)
           COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`guild`, `name`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;

-- ----------------------------
-- Table structure for playlist
-- ----------------------------
DROP TABLE IF EXISTS `playlist`;
CREATE TABLE `playlist` (
  `id`           INT(11)                 NOT NULL AUTO_INCREMENT,
  `youtubecode`  VARCHAR(32)
                 COLLATE utf8_unicode_ci NOT NULL,
  `filename`     VARCHAR(64)
                 COLLATE utf8_unicode_ci NOT NULL,
  `title`        VARCHAR(128)
                 COLLATE utf8_unicode_ci          DEFAULT NULL,
  `artist`       VARCHAR(128)
                 COLLATE utf8_unicode_ci          DEFAULT NULL,
  `lastplaydate` INT(11)                 NOT NULL DEFAULT '0',
  `banned`       INT(11)                 NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 124
  DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;

-- ----------------------------
-- Table structure for servers
-- ----------------------------
DROP TABLE IF EXISTS `servers`;
CREATE TABLE `servers` (
  `id`         INT(11)                 NOT NULL AUTO_INCREMENT,
  `discord_id` VARCHAR(255)
               COLLATE utf8_unicode_ci NOT NULL,
  `name`       VARCHAR(128)
               COLLATE utf8_unicode_ci          DEFAULT NULL,
  `owner`      INT(11)                 NOT NULL,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 6
  DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;

-- ----------------------------
-- Table structure for template_texts
-- ----------------------------
DROP TABLE IF EXISTS `template_texts`;
CREATE TABLE `template_texts` (
  `id`        INT(11)                 NOT NULL AUTO_INCREMENT,
  `keyphrase` VARCHAR(50)
              COLLATE utf8_unicode_ci NOT NULL,
  `text`      VARCHAR(255)
              COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `txt_search` (`keyphrase`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 62
  DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id`         INT(11)                 NOT NULL AUTO_INCREMENT,
  `discord_id` VARCHAR(64)
               COLLATE utf8_unicode_ci NOT NULL,
  `name`       VARCHAR(128)
               COLLATE utf8_unicode_ci          DEFAULT NULL,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 6
  DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;

-- ----------------------------
-- Function structure for levenshtein
-- ----------------------------
DROP FUNCTION IF EXISTS `levenshtein`;
DELIMITER ;;
CREATE DEFINER =`root`@`localhost` FUNCTION `levenshtein`(s1 VARCHAR(255), s2 VARCHAR(255))
  RETURNS INT(11)
DETERMINISTIC
  BEGIN
    DECLARE s1_len, s2_len, i, j, c, c_temp, cost INT;
    DECLARE s1_char CHAR;
    -- max strlen=255
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
  END;;
DELIMITER ;

-- ----------------------------
-- Function structure for levenshtein_ratio
-- ----------------------------
DROP FUNCTION IF EXISTS `levenshtein_ratio`;
DELIMITER ;;
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
  END;;
DELIMITER ;