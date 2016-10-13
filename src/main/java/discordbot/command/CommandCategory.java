package discordbot.command;

public enum CommandCategory {
	BOT_ADMINISTRATION("bot_administration", ":monkey:", "Bot administration"),
	INFORMATIVE("informative", ":information_source:", "Information"),
	ADMINISTRATIVE("administrative", ":oncoming_police_car:", "Administration"),
	MUSIC("music", ":musical_note:", "Music"),
	ECONOMY("economy", ":moneybag:", "Money"),
	FUN("fun", ":game_die:", "Fun"),
	POE("poe", ":currency_exchange:", "Path of exile"),
	HEARTHSTONE("hearthstone", ":slot_machine:", "Hearthstone"),
	ADVENTURE("adventure", ":feet:", "Adventure"),
	UNKNOWN("nopackage", ":question:", "Misc");
	private final String packageName;
	private final String emoticon;
	private final String displayName;

	CommandCategory(String packageName, String emoticon, String displayName) {

		this.packageName = packageName;
		this.emoticon = emoticon;
		this.displayName = displayName;
	}

	public static CommandCategory fromPackage(String packageName) {
		if (packageName != null) {
			for (CommandCategory cc : values()) {
				if (packageName.equalsIgnoreCase(cc.packageName)) {
					return cc;
				}
			}
		}
		return UNKNOWN;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getEmoticon() {
		return emoticon;
	}
}