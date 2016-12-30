package discordbot.guildsettings.types;

import discordbot.guildsettings.IGuildSettingType;
import net.dv8tion.jda.core.entities.Guild;

/**
 * number between settings type
 * the setting has to be between min, and max (including)
 */
public class NumberBetweenSettingType implements IGuildSettingType {
	private final int min, max;

	/**
	 * @param min minimum value
	 * @param max maximum value (including)
	 */
	public NumberBetweenSettingType(int min, int max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public String typeName() {
		return "enum";
	}

	@Override
	public boolean validate(Guild guild, String value) {
		try {
			int vol = Integer.parseInt(value);
			if (vol >= min && vol <= max) {
				return true;
			}
		} catch (Exception ignored) {
		}
		return false;
	}

	@Override
	public String fromInput(Guild guild, String value) {
		try {
			int vol = Integer.parseInt(value);
			if (vol >= min && vol <= max) {
				return "" + vol;
			}
		} catch (Exception ignored) {
		}
		return "";
	}

	@Override
	public String toDisplay(Guild guild, String value) {
		return value;
	}
}
