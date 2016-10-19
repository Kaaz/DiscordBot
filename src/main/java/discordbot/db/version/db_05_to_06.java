package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * Renamed a couple of tables to match the discord names
 * added the option to ban users, guilds
 * added a few indices to make searching a bit better
 */
public class db_05_to_06 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 5;
	}

	@Override
	public int getToVersion() {
		return 6;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"ALTER TABLE servers ADD banned INT NULL",
				"ALTER TABLE servers RENAME TO guilds",
				"CREATE UNIQUE INDEX users_discord_id_uindex ON users (discord_id)",
				"ALTER TABLE playlist RENAME TO music",
				"ALTER TABLE users ADD banned INT NULL",
				"CREATE UNIQUE INDEX guilds_discord_id_uindex ON guilds (discord_id)"
		};
	}
}