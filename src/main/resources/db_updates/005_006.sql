ALTER TABLE servers
  ADD banned INT NULL;
ALTER TABLE servers
  RENAME TO guilds;
CREATE UNIQUE INDEX users_discord_id_uindex
  ON users (discord_id);
ALTER TABLE playlist
  RENAME TO music;
ALTER TABLE users
  ADD banned INT NULL;
CREATE UNIQUE INDEX guilds_discord_id_uindex
  ON guilds (discord_id);