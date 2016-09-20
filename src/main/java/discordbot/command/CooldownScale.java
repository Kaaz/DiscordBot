package discordbot.command;

/**
 * The scale of the cooldown, is it based per user, channel, guild or global?
 */
public enum CooldownScale {
	USER(1), CHANNEL(2), GUILD(3), GLOBAL(4);

	private final int identifier;

	CooldownScale(int identifier) {

		this.identifier = identifier;
	}

	public int getId() {
		return identifier;
	}
}
