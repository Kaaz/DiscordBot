ALTER TABLE moderation_case
  MODIFY message_id VARCHAR(32);
ALTER TABLE moderation_case
  ADD user_name VARCHAR(64) NULL;
ALTER TABLE moderation_case
  ADD moderator_name VARCHAR(64) NULL;
TRUNCATE TABLE moderation_case;