package discordbot.exceptions;

public class DefaultSettingAlreadyExistsException extends Exception {
	private String s;

	public DefaultSettingAlreadyExistsException(String propertyName) {
		s = "Config property '" + propertyName + "' already exists";
	}

	@Override
	public String toString() {
		return s;
	}
}
