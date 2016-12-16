package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * keep track of whether a file should exist or not
 */
public class db_18_to_19 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 18;
	}

	@Override
	public int getToVersion() {
		return 19;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"ALTER TABLE music ADD file_exists INT DEFAULT 1 NOT NULL"
		};
	}
}