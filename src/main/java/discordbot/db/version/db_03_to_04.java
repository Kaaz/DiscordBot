package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * Track if a server is still active, mostly so it can send a different message the first time it connects to a guild
 */
public class db_03_to_04 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 3;
	}

	@Override
	public int getToVersion() {
		return 4;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"ALTER TABLE servers ADD active INT NULL"
		};
	}
}