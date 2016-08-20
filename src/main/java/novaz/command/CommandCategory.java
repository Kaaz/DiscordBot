package novaz.command;

public enum CommandCategory {
	INFORMATIVE("informative", ":information_source:"),
	ADMINISTRATIVE("administrative", ":oncoming_police_car:"),
	MUSIC("music", ":musical_note:"),
	FUN("fun", ":game_die:"),
	UNKNOWN("nopackage", ":question:");

	private final String packageName;
	private final String emoticon;

	CommandCategory(String packageName, String emoticon) {

		this.packageName = packageName;
		this.emoticon = emoticon;
	}

	public static CommandCategory fromPackage(String packName) {
		if (packName != null) {
			for (CommandCategory cc : values()) {
				if (packName.equalsIgnoreCase(cc.packageName)) {
					return cc;
				}
			}
		}
		return UNKNOWN;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getEmoticon() {
		return emoticon;
	}
}
