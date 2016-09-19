CREATE TABLE commands
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  server INT(11),
  input VARCHAR(50) NOT NULL,
  output TEXT NOT NULL
);
CREATE TABLE servers
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  discord_id VARCHAR(255) NOT NULL,
  name VARCHAR(128),
  owner INT(11) NOT NULL
);
CREATE TABLE template_texts
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  keyphrase VARCHAR(50) NOT NULL,
  text VARCHAR(255) NOT NULL
);
CREATE INDEX txt_search ON template_texts (keyphrase);
CREATE TABLE users
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  discord_id VARCHAR(64) NOT NULL,
  name VARCHAR(128)
);
CREATE TABLE guild_settings
(
  guild INT(11) NOT NULL,
  name VARCHAR(255) NOT NULL,
  config VARCHAR(255) NOT NULL,
  CONSTRAINT `PRIMARY` PRIMARY KEY (guild, name)
);
CREATE TABLE playlist
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  youtubecode VARCHAR(32) NOT NULL,
  filename VARCHAR(64) NOT NULL,
  title VARCHAR(128),
  artist VARCHAR(128),
  lastplaydate INT(11) DEFAULT '0' NOT NULL,
  banned INT(11) DEFAULT '0' NOT NULL,
  youtube_title VARCHAR(255)
);
CREATE TABLE bank_reserverations
(
  id INT(11) PRIMARY KEY NOT NULL,
  bank INT(11) NOT NULL,
  amount INT(11) NOT NULL,
  reason VARCHAR(255),
  reservation_time TIMESTAMP DEFAULT '0000-00-00 00:00:00' NOT NULL
);
CREATE TABLE bank_transactions
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  bank_from INT(11) NOT NULL,
  bank_to INT(11) NOT NULL,
  transaction_date TIMESTAMP DEFAULT '0000-00-00 00:00:00' NOT NULL,
  description VARCHAR(255)
);
CREATE TABLE banks
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  user INT(11) NOT NULL,
  current_balance INT(11),
  created_on TIMESTAMP DEFAULT '0000-00-00 00:00:00' NOT NULL
);
CREATE TABLE command_log
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  user_id INT(11) NOT NULL,
  guild INT(11),
  command VARCHAR(64) NOT NULL,
  args TEXT,
  execute_date TIMESTAMP DEFAULT 'CURRENT_TIMESTAMP' NOT NULL
);
CREATE TABLE poe_token
(
  user_id INT(11) PRIMARY KEY NOT NULL,
  session_id VARCHAR(128)
);
CREATE TABLE subscriptions
(
  server_id INT(11) NOT NULL,
  channel_id INT(11) NOT NULL,
  service_id INT(11) DEFAULT '0' NOT NULL,
  subscribed INT(11),
  CONSTRAINT `PRIMARY` PRIMARY KEY (server_id, channel_id, service_id)
);
CREATE TABLE service_variables
(
  service_id INT(11) NOT NULL,
  variable VARCHAR(64) NOT NULL,
  value VARCHAR(128),
  CONSTRAINT `PRIMARY` PRIMARY KEY (service_id, variable)
);
CREATE TABLE services
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  name VARCHAR(32) NOT NULL,
  display_name VARCHAR(64) NOT NULL,
  description TEXT,
  activated INT(11)
);
CREATE UNIQUE INDEX subscription_service_name_uindex ON services (name);
CREATE TABLE channels
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  discord_id VARCHAR(255),
  server_id INT(11),
  name VARCHAR(255)
);
CREATE UNIQUE INDEX channels_discord_id_uindex ON channels (discord_id);
CREATE TABLE command_cooldown
(
  command VARCHAR(64) NOT NULL,
  target_id VARCHAR(64) DEFAULT '' NOT NULL,
  target_type INT(11) DEFAULT '0' NOT NULL,
  last_time INT(21) NOT NULL,
  CONSTRAINT `PRIMARY` PRIMARY KEY (command, target_id, target_type)
);
CREATE TABLE guild_member
(
  guild_id INT(11) NOT NULL,
  user_id INT(11) NOT NULL,
  join_date TIMESTAMP DEFAULT 'CURRENT_TIMESTAMP' NOT NULL,
  CONSTRAINT `PRIMARY` PRIMARY KEY (guild_id, user_id)
);
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `levenshtein`( s1 VARCHAR(255), s2 VARCHAR(255) ) RETURNS int(11)
DETERMINISTIC
  BEGIN
    DECLARE s1_len, s2_len, i, j, c, c_temp, cost INT;
    DECLARE s1_char CHAR;
    -- max strlen=255 
    DECLARE cv0, cv1 VARBINARY(256);
    SET s1_len = CHAR_LENGTH(s1), s2_len = CHAR_LENGTH(s2), cv1 = 0x00, j = 1, i = 1, c = 0;
    IF s1 = s2 THEN
      RETURN 0;
    ELSEIF s1_len = 0 THEN
      RETURN s2_len;
    ELSEIF s2_len = 0 THEN
      RETURN s1_len;
    ELSE
      WHILE j <= s2_len DO
        SET cv1 = CONCAT(cv1, UNHEX(HEX(j))), j = j + 1;
      END WHILE;
      WHILE i <= s1_len DO
        SET s1_char = SUBSTRING(s1, i, 1), c = i, cv0 = UNHEX(HEX(i)), j = 1;
        WHILE j <= s2_len DO
          SET c = c + 1;
          IF s1_char = SUBSTRING(s2, j, 1) THEN
            SET cost = 0; ELSE SET cost = 1;
          END IF;
          SET c_temp = CONV(HEX(SUBSTRING(cv1, j, 1)), 16, 10) + cost;
          IF c > c_temp THEN SET c = c_temp; END IF;
          SET c_temp = CONV(HEX(SUBSTRING(cv1, j+1, 1)), 16, 10) + 1;
          IF c > c_temp THEN
            SET c = c_temp;
          END IF;
          SET cv0 = CONCAT(cv0, UNHEX(HEX(c))), j = j + 1;
        END WHILE;
        SET cv1 = cv0, i = i + 1;
      END WHILE;
    END IF;
    RETURN c;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `levenshtein_ratio` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `levenshtein_ratio`( s1 VARCHAR(255), s2 VARCHAR(255) ) RETURNS int(11)
DETERMINISTIC
  BEGIN
    DECLARE s1_len, s2_len, max_len INT;
    SET s1_len = LENGTH(s1), s2_len = LENGTH(s2);
    IF s1_len > s2_len THEN
      SET max_len = s1_len;
    ELSE
      SET max_len = s2_len;
    END IF;
    RETURN ROUND((1 - LEVENSHTEIN(s1, s2) / max_len) * 100);
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-09-06 16:28:36
