package discordbot.db.version;

import discordbot.db.IDbVersion;

//message saved as varchar
public class db_27_to_28 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 27;
	}

	@Override
	public int getToVersion() {
		return 28;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"ALTER TABLE moderation_case MODIFY message_id VARCHAR(32)",
				"ALTER TABLE moderation_case ADD user_name VARCHAR(64) NULL",
				"ALTER TABLE moderation_case ADD moderator_name VARCHAR(64) NULL",
				"TRUNCATE TABLE moderation_case"
		};
	}
}