ALTER TABLE reaction_role_message ADD custom_emoji INT DEFAULT 0 NULL;
ALTER TABLE reaction_role_message CHANGE emote_id emoji VARCHAR(32);
ALTER TABLE reaction_role_message MODIFY COLUMN role_id BIGINT(20) AFTER custom_emoji;
