ALTER TABLE `bot_events`
  CHANGE `data` `data` MEDIUMTEXT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `command_log`
  CHANGE `args` `args` MEDIUMTEXT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `commands`
  CHANGE `output` `output` MEDIUMTEXT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `guild_roles_self`
  CHANGE `description` `description` MEDIUMTEXT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `reply_pattern`
  CHANGE `reply` `reply` MEDIUMTEXT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `services`
  CHANGE `description` `description` MEDIUMTEXT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `tags`
  CHANGE `response` `response` TEXT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `bank_reserverations`
  CHANGE `reason` `reason` VARCHAR(191)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `bank_transactions`
  CHANGE `description` `description` VARCHAR(191)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `blacklist_commands`
  CHANGE `command` `command` VARCHAR(64)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `blacklist_commands`
  CHANGE `channel_id` `channel_id` VARCHAR(32)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `bot_events`
  CHANGE `event_group` `event_group` VARCHAR(32)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `bot_events`
  CHANGE `sub_group` `sub_group` VARCHAR(32)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `bot_meta`
  CHANGE `meta_name` `meta_name` VARCHAR(32)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `bot_meta`
  CHANGE `meta_value` `meta_value` VARCHAR(32)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `bot_playing_on`
  CHANGE `guild_id` `guild_id` VARCHAR(32)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `bot_playing_on`
  CHANGE `channel_id` `channel_id` VARCHAR(32)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `channels`
  CHANGE `discord_id` `discord_id` VARCHAR(191)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `channels`
  CHANGE `name` `name` VARCHAR(191)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `command_cooldown`
  CHANGE `command` `command` VARCHAR(64)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `command_cooldown`
  CHANGE `target_id` `target_id` VARCHAR(64)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `command_log`
  CHANGE `command` `command` VARCHAR(64)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `commands`
  CHANGE `input` `input` VARCHAR(50)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `guild_roles_self`
  CHANGE `discord_role_id` `discord_role_id` VARCHAR(32)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `guild_roles_self`
  CHANGE `role_name` `role_name` VARCHAR(128)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `guild_settings`
  CHANGE `name` `name` VARCHAR(32)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `guild_settings`
  CHANGE `config` `config` VARCHAR(191)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `guilds`
  CHANGE `discord_id` `discord_id` VARCHAR(191)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `guilds`
  CHANGE `name` `name` VARCHAR(128)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `music`
  CHANGE `youtubecode` `youtubecode` VARCHAR(32)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `music`
  CHANGE `filename` `filename` VARCHAR(191)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `music`
  CHANGE `youtube_title` `youtube_title` VARCHAR(128)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `music`
  CHANGE `artist` `artist` VARCHAR(128)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `music`
  CHANGE `title` `title` VARCHAR(128)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `playlist`
  CHANGE `title` `title` VARCHAR(191)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `poe_token`
  CHANGE `session_id` `session_id` VARCHAR(64)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `ranks`
  CHANGE `code_name` `code_name` VARCHAR(32)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `ranks`
  CHANGE `full_name` `full_name` VARCHAR(191)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `reply_pattern`
  CHANGE `tag` `tag` VARCHAR(64)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `reply_pattern`
  CHANGE `pattern` `pattern` VARCHAR(191)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `service_variables`
  CHANGE `variable` `variable` VARCHAR(64)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `service_variables`
  CHANGE `value` `value` VARCHAR(128)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `services`
  CHANGE `name` `name` VARCHAR(32)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `services`
  CHANGE `display_name` `display_name` VARCHAR(64)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `tags`
  CHANGE `tag_name` `tag_name` VARCHAR(32)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `template_texts`
  CHANGE `keyphrase` `keyphrase` VARCHAR(50)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `template_texts`
  CHANGE `text` `text` VARCHAR(191)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `users`
  CHANGE `discord_id` `discord_id` VARCHAR(64)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `users`
  CHANGE `name` `name` VARCHAR(128)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
ALTER TABLE `banks`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `bank_reserverations`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `bank_transactions`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `blacklist_commands`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `bot_events`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `bot_meta`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `bot_playing_on`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `channels`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `commands`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `command_cooldown`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `command_log`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `guilds`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `guild_member`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `guild_roles_self`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `guild_settings`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `music`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `music_log`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `music_votes`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `playlist`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `playlist_item`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `poe_token`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `ranks`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `reply_pattern`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `services`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
ALTER TABLE `service_variables`
  CONVERT TO CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;