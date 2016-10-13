package discordbot.permission;

/**
 *
 */
public enum SimpleRank {
	BANNED_USER(),
	BOT(),
	USER(),
	GUILD_ADMIN(),
	GUILD_OWNER(),
	BOT_ADMIN(),
	CONTRIBUTOR(),
	CREATOR();

	public boolean isAtLeast(SimpleRank rank) {
		return this.ordinal() >= rank.ordinal();
	}
}
