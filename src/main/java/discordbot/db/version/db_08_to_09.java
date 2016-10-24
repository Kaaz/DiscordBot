package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * the ability for users to rate songs
 */
public class db_08_to_09 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 8;
	}

	@Override
	public int getToVersion() {
		return 9;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				" CREATE TABLE music_votes " +
						" (	song_id INT NOT NULL, " +
						"		user_id INT NOT NULL," +
						"		vote INT NOT NULL," +
						"		created_on TIMESTAMP NOT NULL," +
						"		CONSTRAINT music_votes_song_id_user_id_pk PRIMARY KEY (song_id, user_id) )"
		};
	}
}