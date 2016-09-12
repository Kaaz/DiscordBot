package novaz.command;

public enum CooldownScale {
	USER(1), CHANNEL(2), GUILD(3);

	private final int identifier;

	CooldownScale(int identifier) {

		this.identifier = identifier;
	}

	public int getId() {
		return identifier;
	}
}
