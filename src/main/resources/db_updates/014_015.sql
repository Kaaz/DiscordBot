ALTER TABLE music
  ADD play_count INT NOT NULL;
ALTER TABLE music
  ADD last_manual_playdate INT NOT NULL;
ALTER TABLE users
  ADD commands_used INT NOT NULL;