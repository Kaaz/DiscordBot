package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * guild mod case
 */
public class db_24_to_25 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 24;
	}

	@Override
	public int getToVersion() {
		return 25;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"DROP INDEX moderation_case_guild_id_user_id_pk ON moderation_case",
				"DROP INDEX moderation_case_guild_id_message_id_pk ON moderation_case",
				"CREATE INDEX moderation_case_guild_id_message_id_pk ON moderation_case(guild_id, message_id)",
				"DROP INDEX moderation_case_user_id_message_id_pk ON moderation_case",
				"CREATE INDEX moderation_case_user_id_message_id_pk ON moderation_case(user_id, message_id)",
		};
	}
}