ALTER TABLE template_texts
  ADD guild_id INT NOT NULL;
ALTER TABLE template_texts
  MODIFY COLUMN guild_id INT NOT NULL
  AFTER id;