package discordbot.modules.pathofexile;

import discordbot.modules.pathofexile.obj.PoEItem;

/**
 * Path of exile
 */
public interface IPoEAnalyzePart {

	/**
	 * wheneter or not the paragraph is analyzeable by the class
	 *
	 * @return yes or no
	 */
	boolean canAnalyze(String text);

	/**
	 * @param item the PoEitem to fill the data and return
	 * @param text the text to analyze
	 * @return the item filled with the extra data the analyzer can
	 */
	PoEItem analyze(PoEItem item, String text);
}
