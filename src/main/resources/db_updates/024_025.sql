DROP INDEX moderation_case_guild_id_user_id_pk
ON moderation_case;
DROP INDEX moderation_case_guild_id_message_id_pk
ON moderation_case;
CREATE INDEX moderation_case_guild_id_message_id_pk
  ON moderation_case (guild_id, message_id);
DROP INDEX moderation_case_user_id_message_id_pk
ON moderation_case;
CREATE INDEX moderation_case_user_id_message_id_pk
  ON moderation_case (user_id, message_id);