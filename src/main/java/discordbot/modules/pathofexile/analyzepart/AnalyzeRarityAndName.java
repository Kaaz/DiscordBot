/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
