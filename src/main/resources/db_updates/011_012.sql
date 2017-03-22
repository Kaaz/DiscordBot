CREATE TABLE music_log (
  id        INT(11) PRIMARY KEY                 NOT NULL AUTO_INCREMENT,
  music_id  INT(11) DEFAULT '0'                 NOT NULL,
  guild_id  INT(11) DEFAULT '0'                 NOT NULL,
  user_id   INT(11),
  play_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX music_log_guild_id_index
  ON music_log (guild_id);
CREATE INDEX music_log_guild_id_music_id_index
  ON music_log (guild_id, music_id);
CREATE INDEX music_log_music_id_index
  ON music_log (music_id);
CREATE INDEX music_filename_unique_index
  ON music (filename);
CREATE INDEX music_youtubecode_unique_index
  ON music (youtubecode);