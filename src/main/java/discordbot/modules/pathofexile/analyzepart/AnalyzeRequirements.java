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
                default:
                    break;
            }
        }
        return item;
    }
}
