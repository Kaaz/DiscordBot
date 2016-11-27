package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * enabling/disabling commands per guild
 */
public class db_17_to_18 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 17;
	}

	@Override
	public int getToVersion() {
		return 18;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				" CREATE TABLE blacklist_commands ( " +
						" guild_id INT NOT NULL, " +
						" command VARCHAR(64) NOT NULL, " +
						" CONSTRAINT blacklist_commands_guild_id_command_pk PRIMARY KEY (guild_id, command) " +
						" )",
				"CREATE INDEX blacklist_commands_guild_id_index ON blacklist_commands (guild_id)"
		};
	}
}