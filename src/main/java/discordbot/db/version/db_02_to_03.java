package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * for music; Allow for a longer filename (up to 255)
 */
public class db_02_to_03 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 2;
	}

	@Override
	public int getToVersion() {
		return 3;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"ALTER TABLE playlist MODIFY filename VARCHAR(255) NOT NULL"
		};
	}
}