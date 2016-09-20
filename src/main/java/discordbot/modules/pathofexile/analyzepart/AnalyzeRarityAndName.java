package discordbot.modules.pathofexile.analyzepart;

import discordbot.modules.pathofexile.IPoEAnalyzePart;
import discordbot.modules.pathofexile.enums.Rarity;
import discordbot.modules.pathofexile.obj.PoEItem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalyzeRarityAndName implements IPoEAnalyzePart {
	private final Pattern rarityPattern = Pattern.compile("Rarity: ([A-Za-z]{2,})");

	@Override
	public boolean canAnalyze(String text) {
		return text.startsWith("Rarity:");
	}

	@Override
	public PoEItem analyze(PoEItem item, String text) {
		Matcher rarityMather = rarityPattern.matcher(text);
		if (rarityMather.find()) {
			item.rarity = Rarity.fromString(rarityMather.group(1));
		}
		String[] lines = text.split("\n");
		for (String line : lines) {
			System.out.println(">>" + line + "<<");
		}
		if (lines.length >= 3) {
			item.base = lines[2];
			item.name = lines[1];
		}
		return item;
	}
}
