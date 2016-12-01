package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * Table for auto replies to matching patterns in text
 */
public class db_04_to_05 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 4;
	}

	@Override
	public int getToVersion() {
		return 5;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"CREATE TABLE reply_pattern ( " +
						" id INT PRIMARY KEY AUTO_INCREMENT, " +
						" guild_id INT, " +
						" user_id INT, " +
						" tag VARCHAR(64), " +
						" pattern VARCHAR(255) NOT NULL, " +
						" reply TEXT, " +
						" created_on TIMESTAMP, " +
						" cooldown INT " +
						" )"
		};
	}
}