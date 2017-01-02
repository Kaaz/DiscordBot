package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * guild moderation cases
 */
public class db_23_to_24 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 23;
	}

	@Override
	public int getToVersion() {
		return 24;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"CREATE TABLE moderation_case( " +
						" id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT," +
						" guild_id INT(11) NOT NULL," +
						" user_id INT(11) NOT NULL," +
						" moderator INT(11)," +
						" message_id INT(21) NOT NULL," +
						" created_at DATETIME NOT NULL," +
						" reason TEXT NOT NULL," +
						" punishment INT(11) NOT NULL," +
						" expires DATETIME," +
						" active INT(11) NOT NULL )",
				"CREATE UNIQUE INDEX moderation_case_guild_id_user_id_pk ON moderation_case (guild_id, user_id)",
				"CREATE UNIQUE INDEX moderation_case_guild_id_message_id_pk ON moderation_case (guild_id, message_id)",
				"CREATE UNIQUE INDEX moderation_case_user_id_message_id_pk ON moderation_case (user_id, message_id)",
		};
	}
}