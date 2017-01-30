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

package discordbot.modules.pathofexile;

import discordbot.modules.pathofexile.obj.PoEItem;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ItemAnalyzer {
    private static final String paragraphSplitter = "--------";
    private final Pattern levelRequirementPattern = Pattern.compile("\nLevel: ([0-9]+)");
    private PoEItem item;
    private List<IPoEAnalyzePart> analyzers = new ArrayList<>();
    private List<Boolean> hasAnalyzed = new ArrayList<>();

    public ItemAnalyzer() {
        loadAnalyzeParts();
    }

    private void loadAnalyzeParts() {
        Reflections reflections = new Reflections("discordbot.modules.pathofexile.analyzepart");
        Set<Class<? extends IPoEAnalyzePart>> classes = reflections.getSubTypesOf(IPoEAnalyzePart.class);
        for (Class<? extends IPoEAnalyzePart> clazz : classes) {
            try {
                IPoEAnalyzePart obj = clazz.getConstructor().newInstance();
                analyzers.add(obj);
                hasAnalyzed.add(false);

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public PoEItem attemptToANALyze(String toanalyze) {
        item = new PoEItem();
        String[] paragraphs = breakIntoParts(toanalyze);
        int totalAnalyzers = analyzers.size();
        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            boolean paragraphIsAnalyzed = false;
            for (int i = 0; i < totalAnalyzers; i++) {
                if (!hasAnalyzed.get(i)) {
                    if (analyzers.get(i).canAnalyze(paragraph)) {
                        item = analyzers.get(i).analyze(item, paragraph);
                        hasAnalyzed.set(i, true);
                        paragraphIsAnalyzed = true;
                        break;
                    }
                }
            }
            if (!paragraphIsAnalyzed) {
                System.out.println("!!!!!!!!!!!!!!!!");
                System.out.println(paragraph);
                System.out.println("!!!!!!!!!!!!!!!!");
            }
        }
        return item;
    }

    private String[] breakIntoParts(String toanalyze) {
        return toanalyze.split(paragraphSplitter);
    }
}
