package novaz.modules.pathofexile.obj;

import novaz.modules.pathofexile.enums.Rarity;

/**
 * Created on 2-9-2016
 */
public class PoEItem {
	public Rarity rarity = Rarity.UNKNOWN;
	public int itemLevel = 0;
	public String base = "";
	public String name = "";
	public int requirementLevel = 0;
	public int requirementDex = 0;
	public int requirementStr = 0;
	public int requirementInt = 0;

	@Override
	public String toString() {
		return "PoEItem{" +
				"rarity=" + rarity +
				", itemLevel=" + itemLevel +
				", requirementLevel=" + requirementLevel +
				", base='" + base + '\'' +
				", name='" + name + '\'' +
				", requirementDex=" + requirementDex +
				", requirementStr=" + requirementStr +
				", requirementInt=" + requirementInt +
				'}';
	}
}
