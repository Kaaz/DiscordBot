package discordbot.guildsettings.types;

import discordbot.guildsettings.IGuildSettingType;
import discordbot.util.Emojibet;
import discordbot.util.Misc;
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
		return value != null && (Misc.isFuzzyTrue(value) || Misc.isFuzzyFalse(value));
	}

	@Override
	public String fromInput(Guild guild, String value) {
		return Misc.isFuzzyTrue(value) ? "true" : "false";
	}

	@Override
	public String toDisplay(Guild guild, String value) {
		return value.equals("true") ? Emojibet.OKE_SIGN : Emojibet.X;
	}
}
