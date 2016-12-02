package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * init self assignable roles
 */
public class db_19_to_20 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 19;
	}

	@Override
	public int getToVersion() {
		return 20;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"CREATE TABLE guild_roles_self ( " +
						" guild_id INT NOT NULL, " +
						" discord_role_id VARCHAR(32)) ",
				"ALTER TABLE guild_roles_self ADD description TEXT NULL",
				"ALTER TABLE guild_roles_self ADD role_name VARCHAR(128) NULL",
				"ALTER TABLE guild_roles_self ADD PRIMARY KEY (guild_id, discord_role_id)"
		};
	}
}