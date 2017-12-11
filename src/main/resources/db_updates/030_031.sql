CREATE TABLE reaction_role_key
(
  guild_id INT(11),
  message_key VARCHAR(32) NOT NULL,
  channel_id BIGINT(20),
  message TEXT,
  message_id BIGINT(20),
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT
);
CREATE UNIQUE INDEX reaction_role_key_guild_id_message_key_uindex ON reaction_role_key (guild_id, message_key);

CREATE TABLE reaction_role_message
(
  id INT(11) PRIMARY KEY NOT NULL,
  reaction_role_key_id INT(11),
  emote_id BIGINT(20),
  role_id BIGINT(20)
);