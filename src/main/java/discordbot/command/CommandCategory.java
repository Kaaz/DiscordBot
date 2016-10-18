package discordbot.command;

import discordbot.permission.SimpleRank;

public enum CommandCategory {
	CREATOR("creator", ":man_in_tuxedo:", "Bot administration administration", SimpleRank.CREATOR),
	BOT_ADMINISTRATION("bot_administration", ":monkey:", "Bot administration", SimpleRank.BOT_ADMIN),
	ADMINISTRATIVE("administrative", ":oncoming_police_car:", "Administration", SimpleRank.GUILD_ADMIN),
	INFORMATIVE("informative", ":information_source:", "Information"),
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
	private final SimpleRank rankRequired;

	CommandCategory(String packageName, String emoticon, String displayName) {

		this.packageName = packageName;
		this.emoticon = emoticon;
		this.displayName = displayName;
		this.rankRequired = SimpleRank.USER;
	}

	CommandCategory(String packageName, String emoticon, String displayName, SimpleRank rankRequired) {

		this.packageName = packageName;
		this.emoticon = emoticon;
		this.displayName = displayName;
		this.rankRequired = rankRequired;
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

	public SimpleRank getRankRequired() {
		return rankRequired;
	}
}