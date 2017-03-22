CREATE TABLE bot_playing_on (
  guild_id   VARCHAR(32),
  channel_id VARCHAR(32),
  CONSTRAINT bot_playing_on_pk PRIMARY KEY (guild_id, channel_id)
);