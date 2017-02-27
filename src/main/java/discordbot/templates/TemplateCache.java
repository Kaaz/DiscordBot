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

package discordbot.templates;

import discordbot.db.WebDb;
import discordbot.main.Config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class TemplateCache {
    private static Random rng = new Random();
    //map <{template-key}, {list-of-options}>
    static private final Map<String, List<String>> dictionary = new ConcurrentHashMap<>();

    //map <{guild-id}, map<{template-key}, {list-of-options}>
    static private final ConcurrentHashMap<Integer, Map<String, List<String>>> guildDictionary = new ConcurrentHashMap<>();

    public static synchronized void initialize() {
        dictionary.clear();
        guildDictionary.clear();
        try (ResultSet rs = WebDb.get().select("SELECT id,guild_id, keyphrase, text FROM template_texts WHERE guild_id = 0")) {
            while (rs.next()) {
                String keyphrase = rs.getString("keyphrase");
                if (!Templates.isValidTemplate(keyphrase)) {
                    continue;
                }
                if (!dictionary.containsKey(keyphrase)) {
                    dictionary.put(keyphrase, new ArrayList<>());
                }
                dictionary.get(keyphrase).add(rs.getString("text"));
            }
            rs.getStatement().close();
        } catch (SQLException e) {
            e.getStackTrace();
        }
    }

    public static String getGlobal(String keyPhrase) {
        if (!Config.SHOW_KEYPHRASE) {
            List<String> list = dictionary.get(keyPhrase);
            return list.get(rng.nextInt(list.size()));
        }
        return "**`" + keyPhrase + "`**";
    }
}
