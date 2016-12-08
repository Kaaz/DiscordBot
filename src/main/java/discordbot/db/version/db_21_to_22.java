package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * track duration in music
 */
public class db_21_to_22 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 21;
	}

	@Override
	public int getToVersion() {
		return 22;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"ALTER TABLE music ADD duration INT DEFAULT 0 NOT NULL",
		};
	}
}