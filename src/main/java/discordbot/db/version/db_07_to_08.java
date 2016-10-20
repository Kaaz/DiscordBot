package discordbot.db.version;

import discordbot.db.IDbVersion;

public class db_07_to_08 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 7;
	}

	@Override
	public int getToVersion() {
		return 8;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"CREATE TABLE bot_events ( " +
						" id INT PRIMARY KEY AUTO_INCREMENT, " +
						" created_on TIMESTAMP NOT NULL," +
						" event_group VARCHAR(32) NOT NULL," +
						" sub_group VARCHAR(32)," +
						" data TEXT )"
		};
	}
}