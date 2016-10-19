package discordbot.db.version;

import discordbot.db.IDbVersion;

public class db_07_to_08 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 6;
	}

	@Override
	public int getToVersion() {
		return 7;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"CREATE TABLE user_rank ( user_id INT, rank_type INT )",
		};
	}
}