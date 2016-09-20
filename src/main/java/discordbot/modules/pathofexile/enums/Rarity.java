package discordbot.modules.pathofexile.enums;

/**
 * Created on 2-9-2016
 */
public enum Rarity {
	COMMON("Common"),
	UNCOMMON("Uncommon"),
	RARE("Rare"),
	UNIQUE("Unique"),
	UNKNOWN("Unknown");

	private final String displayName;

	Rarity(String displayName) {

		this.displayName = displayName;
	}

	public static Rarity fromString(String rarityName) {
		if (rarityName != null) {
			for (Rarity rarity : values()) {
				if (rarityName.equalsIgnoreCase(rarity.displayName)) {
					return rarity;
				}
			}
		}
		return UNKNOWN;
	}
}
