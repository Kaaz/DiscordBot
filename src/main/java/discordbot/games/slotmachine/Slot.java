package discordbot.games.slotmachine;

public enum Slot {
	SEVEN("Seven", ":seven:", 50),
	CROWN("Crown", ":crown:", 15),
	BELL("Bell", ":bell:", 15),
	BAR("Bar", ":chocolate_bar:", 10),
	CHERRY("Cherry", ":cherries:", 10),
	MELON("Melon", ":melon:", 5);

	private final String name;
	private final String emote;
	private final int triplePayout;

	Slot(String name, String emote, int triplePayout) {

		this.name = name;
		this.emote = emote;
		this.triplePayout = triplePayout;
	}

	public int getTriplePayout() {
		return triplePayout;
	}

	public String getEmote() {
		return emote;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return emote;
	}
}
