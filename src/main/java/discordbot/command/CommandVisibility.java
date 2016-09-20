package discordbot.command;

/**
 * Visibility of a command
 */
public enum CommandVisibility {
	PRIVATE(), PUBLIC(), BOTH();

	public boolean isForPrivate() {
		return this.equals(PRIVATE) || this.equals(BOTH);
	}

	public boolean isForPublic() {
		return this.equals(PUBLIC) || this.equals(BOTH);
	}
}
