package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * saving some info on shutdown
 * Save the channels where the bot was playing on, to resume again on startup.
 */
public class db_12_to_13 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 12;
	}

	@Override
	public int getToVersion() {
		return 13;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"CREATE TABLE bot_playing_on ( " +
						"guild_id VARCHAR(32), " +
						"channel_id VARCHAR(32), " +
						"CONSTRAINT bot_playing_on_pk PRIMARY KEY (guild_id, channel_id))",

		};
	}
}