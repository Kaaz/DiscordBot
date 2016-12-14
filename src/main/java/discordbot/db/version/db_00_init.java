package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * initialize the whole thing
 */
public class db_00_init implements IDbVersion {
	@Override
	public int getFromVersion() {
		return -1;
	}

	@Override
	public int getToVersion() {
		return 0;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"DROP TABLE IF EXISTS `banks`",
				"CREATE TABLE IF NOT EXISTS `banks`( " +
						"`id`INT(11) NOT NULL AUTO_INCREMENT, " +
						"`user`INT(11) NOT NULL, " +
						"`current_balance` INT(11) DEFAULT NULL, " +
						"`created_on` TIMESTAMP NULL DEFAULT '0000-00-00 00:00:00', " +
						"PRIMARY KEY(`id`) ) " +
						"ENGINE = InnoDB " +
						"DEFAULT CHARSET = latin1 " +
						"AUTO_INCREMENT = 1 ",
				"DROP TABLE IF EXISTS `bank_reserverations`",
				"CREATE TABLE IF NOT EXISTS `bank_reserverations` (\n" +
						"  `id`               INT(11)   NOT NULL,\n" +
						"  `bank`             INT(11)   NOT NULL,\n" +
						"  `amount`           INT(11)   NOT NULL,\n" +
						"  `reason`           VARCHAR(255)       DEFAULT NULL,\n" +
						"  `reservation_time` TIMESTAMP NULL DEFAULT '0000-00-00 00:00:00',\n" +
						"  PRIMARY KEY (`id`) )\n" +
						"  ENGINE = InnoDB\n" +
						"  DEFAULT CHARSET = latin1",
				"DROP TABLE IF EXISTS `bank_transactions`\n",
				"CREATE TABLE IF NOT EXISTS `bank_transactions` (\n" +
						"  `id`               INT(11)   NOT NULL AUTO_INCREMENT,\n" +
						"  `bank_from`        INT(11)   NOT NULL,\n" +
						"  `bank_to`          INT(11)   NOT NULL,\n" +
						"  `transaction_date` TIMESTAMP NULL DEFAULT '0000-00-00 00:00:00',\n" +
						"  `description`      VARCHAR(255)       DEFAULT NULL,\n" +
						"  PRIMARY KEY (`id`)\n" +
						")\n" +
						"  ENGINE = InnoDB\n" +
						"  DEFAULT CHARSET = latin1\n" +
						"  AUTO_INCREMENT = 1",
				"DROP TABLE IF EXISTS `channels`\n",
				"CREATE TABLE IF NOT EXISTS `channels` (\n" +
						"  `id`         INT(11) NOT NULL AUTO_INCREMENT,\n" +
						"  `discord_id` VARCHAR(255)     DEFAULT NULL,\n" +
						"  `server_id`  INT(11)          DEFAULT NULL,\n" +
						"  `name`       VARCHAR(255)     DEFAULT NULL,\n" +
						"  PRIMARY KEY (`id`),\n" +
						"  UNIQUE KEY `channels_discord_id_uindex` (`discord_id`)\n" +
						")\n" +
						"  ENGINE = InnoDB\n" +
						"  DEFAULT CHARSET = latin1\n" +
						"  AUTO_INCREMENT = 1",
				"DROP TABLE IF EXISTS `commands`\n",
				"CREATE TABLE IF NOT EXISTS `commands` (\n" +
						"  `id`     INT(11)     NOT NULL AUTO_INCREMENT,\n" +
						"  `server` INT(11)              DEFAULT NULL,\n" +
						"  `input`  VARCHAR(50) NOT NULL,\n" +
						"  `output` TEXT        NOT NULL,\n" +
						"  PRIMARY KEY (`id`)\n" +
						")\n" +
						"  ENGINE = InnoDB\n" +
						"  DEFAULT CHARSET = latin1\n" +
						"  AUTO_INCREMENT = 1",
				"DROP TABLE IF EXISTS `command_cooldown`\n",
				"CREATE TABLE IF NOT EXISTS `command_cooldown` (\n" +
						"  `command`     VARCHAR(64) NOT NULL,\n" +
						"  `target_id`   VARCHAR(64) NOT NULL DEFAULT '',\n" +
						"  `target_type` INT(11)     NOT NULL DEFAULT '0',\n" +
						"  `last_time`   INT(21)     NOT NULL,\n" +
						"  PRIMARY KEY (`command`, `target_id`, `target_type`)\n" +
						")\n" +
						"  ENGINE = InnoDB\n" +
						"  DEFAULT CHARSET = latin1",
				"DROP TABLE IF EXISTS `command_log`\n",
				"CREATE TABLE IF NOT EXISTS `command_log` (\n" +
						"  `id`           INT(11)     NOT NULL AUTO_INCREMENT,\n" +
						"  `user_id`      INT(11)     NOT NULL,\n" +
						"  `guild`        INT(11)              DEFAULT NULL,\n" +
						"  `command`      VARCHAR(64) NOT NULL,\n" +
						"  `args`         TEXT,\n" +
						"  `execute_date` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,\n" +
						"  PRIMARY KEY (`id`)\n" +
						")\n" +
						"  ENGINE = InnoDB\n" +
						"  DEFAULT CHARSET = latin1\n" +
						"  AUTO_INCREMENT = 1",
				"DROP TABLE IF EXISTS `guild_member`\n",
				"CREATE TABLE IF NOT EXISTS `guild_member` (\n" +
						"  `guild_id`  INT(11)   NOT NULL,\n" +
						"  `user_id`   INT(11)   NOT NULL,\n" +
						"  `join_date` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,\n" +
						"  PRIMARY KEY (`guild_id`, `user_id`)\n" +
						")\n" +
						"  ENGINE = InnoDB\n" +
						"  DEFAULT CHARSET = latin1",
				"DROP TABLE IF EXISTS `guild_settings`\n",
				"CREATE TABLE IF NOT EXISTS `guild_settings` (\n" +
						"  `guild`  INT(11)      NOT NULL,\n" +
						"  `name`   VARCHAR(255) NOT NULL,\n" +
						"  `config` VARCHAR(255) NOT NULL,\n" +
						"  PRIMARY KEY (`guild`, `name`)\n" +
						")\n" +
						"  ENGINE = InnoDB\n" +
						"  DEFAULT CHARSET = latin1;\n",
				"DROP TABLE IF EXISTS playlist\n",
				"DROP TABLE IF EXISTS music\n",
				"CREATE TABLE IF NOT EXISTS `playlist` (\n" +
						"  `id`            INT(11)     NOT NULL AUTO_INCREMENT,\n" +
						"  `youtubecode`   VARCHAR(32) NOT NULL,\n" +
						"  `filename`      VARCHAR(64) NOT NULL,\n" +
						"  `title`         VARCHAR(128)         DEFAULT NULL,\n" +
						"  `artist`        VARCHAR(128)         DEFAULT NULL,\n" +
						"  `lastplaydate`  INT(11)     NOT NULL DEFAULT '0',\n" +
						"  `banned`        INT(11)     NOT NULL DEFAULT '0',\n" +
						"  `youtube_title` VARCHAR(255)         DEFAULT NULL,\n" +
						"  PRIMARY KEY (`id`)\n" +
						")\n" +
						"  ENGINE = InnoDB\n" +
						"  DEFAULT CHARSET = latin1\n" +
						"  AUTO_INCREMENT = 1",
				"DROP TABLE IF EXISTS `poe_token`\n",
				"CREATE TABLE IF NOT EXISTS `poe_token` (\n" +
						"  `user_id`    INT(11) NOT NULL,\n" +
						"  `session_id` VARCHAR(128) DEFAULT NULL,\n" +
						"  PRIMARY KEY (`user_id`)\n" +
						")\n" +
						"  ENGINE = InnoDB\n" +
						"  DEFAULT CHARSET = latin1\n",
				"DROP TABLE IF EXISTS guilds\n",
				"DROP TABLE IF EXISTS servers\n",
				"CREATE TABLE IF NOT EXISTS `servers` (\n" +
						"  `id`         INT(11)      NOT NULL AUTO_INCREMENT,\n" +
						"  `discord_id` VARCHAR(255) NOT NULL,\n" +
						"  `name`       VARCHAR(128)          DEFAULT NULL,\n" +
						"  `owner`      INT(11)      NOT NULL,\n" +
						"  PRIMARY KEY (`id`)\n" +
						")\n" +
						"  ENGINE = InnoDB\n" +
						"  DEFAULT CHARSET = latin1\n" +
						"  AUTO_INCREMENT = 1",
				"DROP TABLE IF EXISTS `services`\n",
				"CREATE TABLE IF NOT EXISTS `services` (\n" +
						"  `id`           INT(11)     NOT NULL AUTO_INCREMENT,\n" +
						"  `name`         VARCHAR(32) NOT NULL,\n" +
						"  `display_name` VARCHAR(64) NOT NULL,\n" +
						"  `description`  TEXT,\n" +
						"  `activated`    INT(11)              DEFAULT NULL,\n" +
						"  PRIMARY KEY (`id`),\n" +
						"  UNIQUE KEY `subscription_service_name_uindex` (`name`)\n" +
						")\n" +
						"  ENGINE = InnoDB\n" +
						"  DEFAULT CHARSET = latin1\n" +
						"  AUTO_INCREMENT = 1\n",
				"DROP TABLE IF EXISTS `service_variables`\n",
				"CREATE TABLE IF NOT EXISTS `service_variables` (\n" +
						"  `service_id` INT(11)     NOT NULL,\n" +
						"  `variable`   VARCHAR(64) NOT NULL,\n" +
						"  `value`      VARCHAR(128) DEFAULT NULL,\n" +
						"  PRIMARY KEY (`service_id`, `variable`)\n" +
						")\n" +
						"  ENGINE = InnoDB\n" +
						"  DEFAULT CHARSET = latin1",
				"DROP TABLE IF EXISTS `subscriptions`\n",
				"CREATE TABLE IF NOT EXISTS `subscriptions` (\n" +
						"  `server_id`  INT(11) NOT NULL,\n" +
						"  `channel_id` INT(11) NOT NULL,\n" +
						"  `service_id` INT(11) NOT NULL DEFAULT '0',\n" +
						"  `subscribed` INT(11)          DEFAULT NULL,\n" +
						"  PRIMARY KEY (`server_id`, `channel_id`, `service_id`)\n" +
						")\n" +
						"  ENGINE = InnoDB\n" +
						"  DEFAULT CHARSET = latin1",
				"DROP TABLE IF EXISTS `template_texts`\n",
				"CREATE TABLE IF NOT EXISTS `template_texts` (\n" +
						"  `id`        INT(11)      NOT NULL AUTO_INCREMENT,\n" +
						"  `keyphrase` VARCHAR(50)  NOT NULL,\n" +
						"  `text`      VARCHAR(255) NOT NULL,\n" +
						"  PRIMARY KEY (`id`),\n" +
						"  KEY `txt_search` (`keyphrase`)\n" +
						")\n" +
						"  ENGINE = InnoDB\n" +
						"  DEFAULT CHARSET = latin1\n" +
						"  AUTO_INCREMENT = 1\n",
				"DROP TABLE IF EXISTS `users`\n",
				"CREATE TABLE IF NOT EXISTS `users` (\n" +
						"  `id`         INT(11)     NOT NULL AUTO_INCREMENT,\n" +
						"  `discord_id` VARCHAR(64) NOT NULL,\n" +
						"  `name`       VARCHAR(128)         DEFAULT NULL,\n" +
						"  PRIMARY KEY (`id`)\n" +
						")\n" +
						"  ENGINE = InnoDB\n" +
						"  DEFAULT CHARSET = latin1\n" +
						"  AUTO_INCREMENT = 1",
		};
	}
}