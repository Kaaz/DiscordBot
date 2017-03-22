ALTER TABLE playlist
  ADD code VARCHAR(32) DEFAULT 'default' NOT NULL;
CREATE UNIQUE INDEX playlist_owner_id_guild_id_code_uindex
  ON playlist (owner_id, guild_id, code);
CREATE INDEX playlist_owner_id_code_index
  ON playlist (owner_id, code);
CREATE INDEX playlist_guild_id_code_index
  ON playlist (guild_id, code);