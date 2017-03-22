ALTER TABLE blacklist_commands
  ADD channel_id VARCHAR(32) NOT NULL;
ALTER TABLE blacklist_commands
  DROP PRIMARY KEY;
ALTER TABLE blacklist_commands
  ADD PRIMARY KEY (guild_id, command, channel_id);
ALTER TABLE blacklist_commands
  ADD enabled INT DEFAULT 0 NULL;