CREATE TABLE bot_events (
  id          INT PRIMARY KEY AUTO_INCREMENT,
  created_on  TIMESTAMP   NOT NULL,
  event_group VARCHAR(32) NOT NULL,
  sub_group   VARCHAR(32),
  data        TEXT
);