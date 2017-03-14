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

package emily.templates;

import emily.db.WebDb;
import emily.db.controllers.CGuild;
import emily.main.BotContainer;

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
                if (!Templates.templateExists(keyphrase)) {
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

    /**
     * returns a list of all texts for specified keyphrase
     *
     * @param keyphrase to return a list of
     * @return list
     */
    public static List<String> getAllFor(String keyphrase) {
        if (dictionary.containsKey(keyphrase)) {
            return dictionary.get(keyphrase);
        }
        return new ArrayList<>();
    }

    public static List<String> getAllFor(int guildId, String keyphrase) {
        if (guildId > 0 && guildDictionary.containsKey(guildId) && guildDictionary.get(guildId).containsKey(keyphrase)) {
            return guildDictionary.get(guildId).get(keyphrase);
        }
        return getAllFor(keyphrase);
    }

    public static synchronized void remove(int guildId, String keyPhrase, String text) {
        if (guildId == 0) {
            removeGlobal(keyPhrase, text);
            return;
        }
        if (guildDictionary.containsKey(guildId) && guildDictionary.get(guildId).containsKey(keyPhrase)) {
            if (guildDictionary.get(guildId).get(keyPhrase).contains(text)) {
                try {
                    WebDb.get().query("DELETE FROM template_texts WHERE keyphrase = ? AND text = ? AND guild_id = ?", keyPhrase, text, guildId);
                    guildDictionary.get(guildId).get(keyPhrase).remove(text);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * deletes a specific entry
     *
     * @param keyPhrase keyphrase
     * @param text      text
     */
    public static synchronized void removeGlobal(String keyPhrase, String text) {
        if (dictionary.containsKey(keyPhrase)) {
            if (dictionary.get(keyPhrase).contains(text)) {
                try {
                    WebDb.get().query("DELETE FROM template_texts WHERE keyphrase = ? AND text = ? ", keyPhrase, text);
                    dictionary.get(keyPhrase).remove(text);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * adds a template for a keyphrase for a guild
     * Only adds the template if the template exists in the dictionary
     *
     * @param guildId   internal guild id
     * @param keyPhrase keyphrase
     * @param text      the text
     */
    public static synchronized boolean add(int guildId, String keyPhrase, String text) {
        if (!Templates.templateExists(keyPhrase)) {
            return false;
        }
        if (guildId == 0) {
            addGlobal(keyPhrase, text);
            return true;
        }
        try {
            WebDb.get().query("INSERT INTO template_texts(guild_id,keyphrase,text) VALUES(?, ?, ?)", guildId, keyPhrase, text);
            if (!guildDictionary.containsKey(guildId)) {
                guildDictionary.put(guildId, new ConcurrentHashMap<>());
            }
            if (!guildDictionary.get(guildId).containsKey(keyPhrase)) {
                guildDictionary.get(guildId).put(keyPhrase, new ArrayList<>());
            }
            guildDictionary.get(guildId).get(keyPhrase).add(text);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * adds a template for a keyphrase
     *
     * @param keyPhrase keyphrase
     * @param text      the text
     */
    public static synchronized void addGlobal(String keyPhrase, String text) {
        try {
            WebDb.get().query("INSERT INTO template_texts(keyphrase,text,guild_id) VALUES(?, ?, 0)", keyPhrase, text);
            if (!dictionary.containsKey(keyPhrase)) {
                dictionary.put(keyPhrase, new ArrayList<>());
            }
            dictionary.get(keyPhrase).add(text);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
