package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * per-user based permission
 */
public class db_16_to_17 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 16;
	}

	@Override
	public int getToVersion() {
		return 17;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"ALTER TABLE users ADD permission_mask INT DEFAULT 0 NOT NULL",
		};
	}
}