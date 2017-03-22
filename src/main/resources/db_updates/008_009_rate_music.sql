CREATE TABLE music_votes
(
  song_id    INT       NOT NULL,
  user_id    INT       NOT NULL,
  vote       INT       NOT NULL,
  created_on TIMESTAMP NOT NULL,
  CONSTRAINT music_votes_song_id_user_id_pk PRIMARY KEY (song_id, user_id)
);