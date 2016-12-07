package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * command blacklist for specific channels
 */
public class db_20_to_21 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 20;
	}

	@Override
	public int getToVersion() {
		return 21;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"ALTER TABLE blacklist_commands ADD channel_id VARCHAR(32)NOT NULL",
				"ALTER TABLE blacklist_commands DROP PRIMARY KEY",
				"ALTER TABLE blacklist_commands ADD PRIMARY KEY(guild_id, command, channel_id)",
		};
	}
}