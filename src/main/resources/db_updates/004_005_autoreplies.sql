CREATE TABLE reply_pattern (
  id         INT PRIMARY KEY AUTO_INCREMENT,
  guild_id   INT,
  user_id    INT,
  tag        VARCHAR(64),
  pattern    VARCHAR(255) NOT NULL,
  reply      TEXT,
  created_on TIMESTAMP,
  cooldown   INT
);