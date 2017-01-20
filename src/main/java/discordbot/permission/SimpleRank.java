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
	SYSTEM_ADMIN(),
	CREATOR();

	public boolean isAtLeast(SimpleRank rank) {
		return this.ordinal() >= rank.ordinal();
	}
	public boolean isHigherThan(SimpleRank rank) {
		return this.ordinal() > rank.ordinal();
	}

	/**
	 * find a rank by name
	 *
	 * @param search the role to search for
	 * @return rank || null
	 */
	public static SimpleRank findRank(String search) {
		for (SimpleRank simpleRank : values()) {
			if (simpleRank.name().equalsIgnoreCase(search)) {
				return simpleRank;
			}
		}
		return null;
	}
}
