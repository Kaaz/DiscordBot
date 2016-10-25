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
	CONTRIBUTOR(),
	BOT_ADMIN(),
	CREATOR();

	public boolean isAtLeast(SimpleRank rank) {
		return this.ordinal() >= rank.ordinal();
	}
}
