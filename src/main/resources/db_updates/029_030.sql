CREATE TABLE bot_stats
(
  id          INT(11) PRIMARY KEY                 NOT NULL AUTO_INCREMENT,
  guild_count BIGINT(20)                          NOT NULL,
  user_count  BIGINT(20)                          NOT NULL,
  music_count BIGINT(20)                          NOT NULL,
  created_on  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX bot_stats_created_on_index
  ON bot_stats (created_on);