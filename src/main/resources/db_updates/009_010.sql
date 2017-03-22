CREATE TABLE playlist (
  id               INT(11) PRIMARY KEY                 NOT NULL AUTO_INCREMENT,
  title            VARCHAR(255)                        NOT NULL,
  owner_id         INT(11)                             NOT NULL,
  guild_id         INT(11)                             NOT NULL,
  visibility_level INT(11)                             NOT NULL,
  edit_type        INT(11)                             NOT NULL,
  create_date      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE playlist_item (
  playlist_id INT(11) DEFAULT '0'NOT NULL,
  music_id    INT(11) DEFAULT '0'NOT NULL,
  last_played INT(21),
  CONSTRAINT `PRIMARY` PRIMARY KEY (playlist_id, music_id)
);