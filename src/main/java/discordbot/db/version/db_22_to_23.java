/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package discordbot.db.version;

import discordbot.db.IDbVersion;
import discordbot.main.Config;

/**
 * move to utf8mb4
 */
public class db_22_to_23 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 22;
	}

	@Override
	public int getToVersion() {
		return 23;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"ALTER DATABASE `" + Config.DB_NAME + "` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci",
				"ALTER TABLE `bot_events` CHANGE `data` `data` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `command_log` CHANGE `args` `args` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `commands` CHANGE `output` `output` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `guild_roles_self` CHANGE `description` `description` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `reply_pattern` CHANGE `reply` `reply` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `services` CHANGE `description` `description` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `tags` CHANGE `response` `response` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `bank_reserverations` CHANGE `reason` `reason` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `bank_transactions` CHANGE `description` `description` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `blacklist_commands` CHANGE `command` `command` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `blacklist_commands` CHANGE `channel_id` `channel_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `bot_events` CHANGE `event_group` `event_group` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `bot_events` CHANGE `sub_group` `sub_group` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `bot_meta` CHANGE `meta_name` `meta_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `bot_meta` CHANGE `meta_value` `meta_value` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `bot_playing_on` CHANGE `guild_id` `guild_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `bot_playing_on` CHANGE `channel_id` `channel_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `channels` CHANGE `discord_id` `discord_id` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `channels` CHANGE `name` `name` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `command_cooldown` CHANGE `command` `command` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `command_cooldown` CHANGE `target_id` `target_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `command_log` CHANGE `command` `command` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `commands` CHANGE `input` `input` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `guild_roles_self` CHANGE `discord_role_id` `discord_role_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `guild_roles_self` CHANGE `role_name` `role_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `guild_settings` CHANGE `name` `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `guild_settings` CHANGE `config` `config` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `guilds` CHANGE `discord_id` `discord_id` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `guilds` CHANGE `name` `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `music` CHANGE `youtubecode` `youtubecode` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `music` CHANGE `filename` `filename` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `music` CHANGE `youtube_title` `youtube_title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `music` CHANGE `artist` `artist` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `music` CHANGE `title` `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `playlist` CHANGE `title` `title` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `poe_token` CHANGE `session_id` `session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `ranks` CHANGE `code_name` `code_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `ranks` CHANGE `full_name` `full_name` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `reply_pattern` CHANGE `tag` `tag` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `reply_pattern` CHANGE `pattern` `pattern` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `service_variables` CHANGE `variable` `variable` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `service_variables` CHANGE `value` `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `services` CHANGE `name` `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `services` CHANGE `display_name` `display_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `tags` CHANGE `tag_name` `tag_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `template_texts` CHANGE `keyphrase` `keyphrase` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `template_texts` CHANGE `text` `text` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `users` CHANGE `discord_id` `discord_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `users` CHANGE `name` `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `banks` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `bank_reserverations` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `bank_transactions` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `blacklist_commands` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `bot_events` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `bot_meta` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `bot_playing_on` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `channels` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `commands` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `command_cooldown` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `command_log` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `guilds` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `guild_member` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `guild_roles_self` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `guild_settings` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `music` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `music_log` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `music_votes` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `playlist` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `playlist_item` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `poe_token` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `ranks` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `reply_pattern` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `services` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
				"ALTER TABLE `service_variables` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
		};
	}
}