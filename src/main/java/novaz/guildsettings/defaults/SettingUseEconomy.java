package novaz.guildsettings.defaults;

import novaz.guildsettings.AbstractGuildSetting;


public class SettingUseEconomy extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "use_economy";
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Use the economy feature?",
				"false -> nope!",
				"true -> yep!"};
	}

	@Override
	public boolean isValidValue(String input) {
		return input != null && (input.equals("true") || input.equals("false"));
	}
}