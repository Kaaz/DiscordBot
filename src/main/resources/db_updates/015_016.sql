ALTER TABLE bot_events
  ADD log_level INT DEFAULT 6 NULL;
ALTER TABLE bot_events
  MODIFY log_level INT(11) NOT NULL DEFAULT '6';