package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * playlist: + playmode
 */
public class db_13_to_14 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 13;
	}

	@Override
	public int getToVersion() {
		return 14;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"ALTER TABLE playlist ADD play_type INT(11) NOT NULL",
		};
	}
}