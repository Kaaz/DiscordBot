package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * playlist: + playmode
 */
public class db_14_to_15 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 14;
	}

	@Override
	public int getToVersion() {
		return 15;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"ALTER TABLE music ADD play_count INT NOT NULL",
				"ALTER TABLE music ADD last_manual_playdate INT NOT NULL",
				"ALTER TABLE users ADD commands_used INT NOT NULL",
		};
	}
}