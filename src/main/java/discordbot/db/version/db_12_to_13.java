package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 *
 */
public class db_12_to_13 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 11;
	}

	@Override
	public int getToVersion() {
		return 12;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"",

		};
	}
}