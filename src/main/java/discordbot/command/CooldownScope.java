package discordbot.command;

/**
 * The scale of the cooldown, is it based per user, channel, guild or global?
 */
public enum CooldownScope {
	USER(1), CHANNEL(2), GUILD(3), GLOBAL(4);

	private final int identifier;

	CooldownScope(int identifier) {

		this.identifier = identifier;
	}

	public int getId() {
		return identifier;
	}
}
