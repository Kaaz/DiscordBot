package discordbot.guildsettings.types;

import discordbot.guildsettings.IGuildSettingType;
import net.dv8tion.jda.core.entities.Guild;

public class NoSettingType implements IGuildSettingType {
	@Override
	public String typeName() {
		return "n/a";
	}

	@Override
	public boolean validate(Guild guild, String value) {
		return true;
	}

	@Override
	public String fromInput(Guild guild, String value) {
		return value;
	}

	@Override
	public String toDisplay(Guild guild, String value) {
		return value;
	}
}
