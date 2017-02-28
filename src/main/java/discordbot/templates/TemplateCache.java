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
import discordbot.db.controllers.CGuild;
import discordbot.main.BotContainer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
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

    public static void initGuildTemplates(BotContainer container) {
        guildDictionary.clear();
        HashSet<Integer> skipList = new HashSet<>();
        HashSet<Integer> whiteList = new HashSet<>();
        try (ResultSet rs = WebDb.get().select("SELECT id,guild_id, keyphrase, text FROM template_texts WHERE guild_id > 0 ORDER BY guild_id")) {
            while (rs.next()) {
                int guildId = rs.getInt("guild_id");
                String keyphrase = rs.getString("keyphrase");
                if (skipList.contains(guildId)) {
                    continue;
                }
                long discordGuildId = Long.parseLong(CGuild.getCachedDiscordId(guildId));
                if (!whiteList.contains(guildId)) {
                    if (container.getShardFor(discordGuildId).client.getGuildById(Long.toString(discordGuildId)) == null) {
                        skipList.add(guildId);
                        continue;
                    } else {
                        whiteList.add(guildId);
                    }
                }
                if (!guildDictionary.containsKey(guildId)) {
                    guildDictionary.put(guildId, new ConcurrentHashMap<>());
                }
                if (!guildDictionary.get(guildId).containsKey(keyphrase)) {
                    guildDictionary.get(guildId).put(keyphrase, new ArrayList<>());
                }
                guildDictionary.get(guildId).get(keyphrase).add(rs.getString("text"));
            }
            rs.getStatement().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void reloadGuild(int guildId) {
        if (guildDictionary.containsKey(guildId)) {
            guildDictionary.remove(guildId);
        }
        try (ResultSet rs = WebDb.get().select("SELECT id,keyphrase, text FROM template_texts WHERE guild_id = ?", guildId)) {
            while (rs.next()) {
                String keyphrase = rs.getString("keyphrase");
                if (!guildDictionary.containsKey(guildId)) {
                    guildDictionary.put(guildId, new ConcurrentHashMap<>());
                }
                if (!guildDictionary.get(guildId).containsKey(keyphrase)) {
                    guildDictionary.get(guildId).put(keyphrase, new ArrayList<>());
                }
                guildDictionary.get(guildId).get(keyphrase).add(rs.getString("text"));

            }
            rs.getStatement().close();
        } catch (SQLException e) {
            e.getStackTrace();
        }
    }

    public static String getGuild(int guildId, String keyPhrase) {
        if (!guildDictionary.containsKey(guildId) || !guildDictionary.get(guildId).containsKey(keyPhrase)) {
            return getGlobal(keyPhrase);
        }
        List<String> list = guildDictionary.get(guildId).get(keyPhrase);
        return list.get(rng.nextInt(list.size()));
    }

    public static String getGlobal(String keyPhrase) {
        if (dictionary.containsKey(keyPhrase)) {
            List<String> list = dictionary.get(keyPhrase);
            return list.get(rng.nextInt(list.size()));
        }
        return "**`" + keyPhrase + "`**";
    }
}
