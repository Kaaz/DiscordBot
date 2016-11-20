package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * bot events, add a log level to it
 */
public class db_15_to_16 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 15;
	}

	@Override
	public int getToVersion() {
		return 16;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"ALTER TABLE bot_events ADD log_level INT DEFAULT 6 NULL",
				"ALTER TABLE bot_events MODIFY log_level INT(11) NOT NULL DEFAULT '6'"
		};
	}
}