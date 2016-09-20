package discordbot.exceptions;

public class TemplateNotSetException extends Exception {
	private String s;

	public TemplateNotSetException(String keyphrase) {
		s = "keyphrase '" + keyphrase + "' is not set to anything";
	}

	@Override
	public String toString() {
		return s;
	}
}
