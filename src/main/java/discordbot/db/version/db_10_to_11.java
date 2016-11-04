package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * introduction of guild-based templates
 */
public class db_10_to_11 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 10;
	}

	@Override
	public int getToVersion() {
		return 11;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"ALTER TABLE template_texts ADD guild_id INT NOT NULL",
				"ALTER TABLE template_texts MODIFY COLUMN guild_id INT NOT NULL AFTER id",
		};
	}
}