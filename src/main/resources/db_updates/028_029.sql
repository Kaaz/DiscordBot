CREATE INDEX tags_tag_name_guild_id_index
  ON tags (tag_name, guild_id);
CREATE INDEX guild_name
  ON tags (tag_name);

CREATE TABLE bets (
  id         INT PRIMARY KEY AUTO_INCREMENT,
  title      VARCHAR(128),
  owner_id   INT      NOT NULL,
  guild_id   INT      NOT NULL,
  created_on DATETIME NOT NULL,
  started_on DATETIME,
  ends_at    DATETIME,
  price      INT
);

CREATE TABLE bet_options (
  id          INT NOT NULL AUTO_INCREMENT,
  bet_id      INT NOT NULL,
  description VARCHAR(128),
  CONSTRAINT bet_options_id_bet_id_pk PRIMARY KEY (id, bet_id)
);

ALTER TABLE bets
  ADD bet_status INT DEFAULT 0 NOT NULL;

CREATE TABLE todo_list (
  id         INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  user_id    INT             NOT NULL,
  guild_id   INT             NOT NULL,
  list_name  VARCHAR(191)    NOT NULL,
  visibility INT
);

CREATE INDEX todo_list_guild_id_index
  ON todo_list (guild_id);
CREATE INDEX todo_list_user_id_index
  ON todo_list (user_id);
CREATE INDEX todo_list_guild_id_user_id_index
  ON todo_list (guild_id, user_id);

CREATE TABLE todo_item (
  id          INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  list_id     INT(11)             NOT NULL,
  description VARCHAR(191),
  checked     INT(11),
  priority    INT(11)
)