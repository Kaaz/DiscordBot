package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * controllers for the tag command
 */
public class db_01_to_02 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 1;
	}

	@Override
	public int getToVersion() {
		return 2;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"CREATE TABLE tags\n" +
						"         (\n" +
						"         id INT PRIMARY KEY AUTO_INCREMENT,\n" +
						"         tag_name VARCHAR(32),\n" +
						"         guild_id INT,\n" +
						"         response TEXT\n" +
						"         )",
				"ALTER TABLE tags ADD user_id INT NULL",
				"ALTER TABLE tags ADD creation_date TIMESTAMP NULL"
		};
	}
}