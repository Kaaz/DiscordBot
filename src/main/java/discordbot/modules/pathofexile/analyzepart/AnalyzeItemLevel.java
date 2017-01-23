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
import discordbot.modules.pathofexile.obj.PoEItem;

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
