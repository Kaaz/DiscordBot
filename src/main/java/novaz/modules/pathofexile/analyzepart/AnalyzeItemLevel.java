package novaz.modules.pathofexile.analyzepart;

import novaz.modules.pathofexile.IPoEAnalyzePart;
import novaz.modules.pathofexile.obj.PoEItem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalyzeItemLevel implements IPoEAnalyzePart {

	private static final Pattern itemLevelpattern = Pattern.compile("Item Level: ([0-9]{1,3})");

	@Override
	public boolean canAnalyze(String text) {
		return text.startsWith("Item Level:");
	}

	@Override
	public PoEItem analyze(PoEItem item, String text) {
		Matcher matcher = itemLevelpattern.matcher(text);
		if (matcher.find()) {
			item.itemLevel = Integer.parseInt(matcher.group(1));
		}
		return item;
	}
}
