CREATE TABLE blacklist_commands (
  guild_id INT         NOT NULL,
  command  VARCHAR(64) NOT NULL,
  CONSTRAINT blacklist_commands_guild_id_command_pk PRIMARY KEY (guild_id, command)
);
CREATE INDEX blacklist_commands_guild_id_index
  ON blacklist_commands (guild_id);