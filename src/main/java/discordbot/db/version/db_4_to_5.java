package discordbot.db.version;

import discordbot.db.IDbVersion;

public class db_4_to_5 implements IDbVersion {
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
				"CREATE TABLE discord.reply_pattern ( " +
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