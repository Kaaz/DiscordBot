package discordbot.guildsettings.types;

import discordbot.guildsettings.IGuildSettingType;
import discordbot.util.Emojibet;
import net.dv8tion.jda.core.entities.Guild;

/**
 * boolean settings type
 * yes/no
 */
public class BooleanSettingType implements IGuildSettingType {
	@Override
	public String typeName() {
		return "toggle";
	}

	@Override
	public boolean validate(Guild guild, String value) {
		return value != null && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"));
	}

	@Override
	public String fromInput(Guild guild, String value) {
		return "true".equalsIgnoreCase(value) ? "true" : "false";
	}

	@Override
	public String toDisplay(Guild guild, String value) {
		return value.equals("true") ? Emojibet.OKE_SIGN : Emojibet.X;
	}
}
