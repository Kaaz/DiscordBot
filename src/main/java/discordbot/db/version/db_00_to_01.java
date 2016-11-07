package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * Start with a meta controllers for meta information such as database version
 */
public class db_00_to_01 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 0;
	}

	@Override
	public int getToVersion() {
		return 1;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"CREATE TABLE bot_meta (meta_name VARCHAR(32) PRIMARY KEY NOT NULL,  meta_value VARCHAR(32));"
		};
	}
}