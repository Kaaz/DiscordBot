package novaz.modules.pathofexile.analyzepart;

import novaz.modules.pathofexile.IPoEAnalyzePart;
import novaz.modules.pathofexile.obj.PoEItem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalyzeRequirements implements IPoEAnalyzePart {

	private static final Pattern requirementPattern = Pattern.compile("([A-Z][a-z]{2,4}): ([0-9]{1,3})");

	@Override
	public boolean canAnalyze(String text) {
		return text.startsWith("Requirements:");
	}

	@Override
	public PoEItem analyze(PoEItem item, String text) {
		Matcher matcher = requirementPattern.matcher(text);
		while (matcher.find()) {
			switch (matcher.group(1)) {
				case "Int":
					item.requirementInt = Integer.parseInt(matcher.group(2));
					break;
				case "Str":
					item.requirementStr = Integer.parseInt(matcher.group(2));
					break;
				case "Dex":
					item.requirementDex = Integer.parseInt(matcher.group(2));
					break;
				case "Level":
					item.requirementLevel = Integer.parseInt(matcher.group(2));
					break;
			}
		}
		return item;
	}
}
