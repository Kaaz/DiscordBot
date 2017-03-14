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

package emily.handler;


import com.google.api.client.repackaged.com.google.common.base.Joiner;
import emily.db.WebDb;
import emily.db.controllers.CBotEvent;
import emily.db.controllers.CGuild;
import emily.main.Config;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the text templates
 * templates are stored in the database,
 */
public class Template {

    static private final Map<String, List<String>> dictionary = new ConcurrentHashMap<>();
    static private final ConcurrentHashMap<Integer, Map<String, List<String>>> guildDictionary = new ConcurrentHashMap<>();
    private static Random rnd = new Random();

    private Template() {
        initialize();
    }

    public static void removeGuild(int guildId) {
        if (guildDictionary.containsKey(guildId)) {
            guildDictionary.remove(guildId);
        }
    }

    /**
     * gets a "random" keyphrase
     *
     * @param keyPhrase keyphrase to return
     * @return a random string out of the options for the keyphrase
     */
    public static String get(String keyPhrase) {
        return get(0, keyPhrase);
    }

    public static String get(MessageChannel channel, String keyPhrase) {
        return get(CGuild.getCachedId(channel), keyPhrase);
    }

    public static String get(int guildId, String keyPhrase) {
        if (!Config.SHOW_KEYPHRASE) {
            if (guildId > 0 && guildDictionary.containsKey(guildId) && guildDictionary.get(guildId).containsKey(keyPhrase)) {
                List<String> list = guildDictionary.get(guildId).get(keyPhrase);
                return list.get(rnd.nextInt(list.size()));
            } else if (dictionary.containsKey(keyPhrase)) {
                List<String> list = dictionary.get(keyPhrase);
                return list.get(rnd.nextInt(list.size()));
            }
        }
        CBotEvent.insert(":warning:", ":label:", String.format("the phrase `%s` is not set!", keyPhrase));
        return "**`" + keyPhrase + "`**";
    }

    /**
     * Formatted version of Template#get(String), but surrounded by String.format
     *
     * @param keyPhrase  keyphrase
     * @param parameters the parameters to put in the keyphrase
     * @return formatted keyphrase
     */
    public static String get(String keyPhrase, Object... parameters) {
        return get(0, keyPhrase, parameters);
    }

    public static String get(MessageChannel channel, String keyPhrase, Object... parameters) {
        return get(CGuild.getCachedId(channel), keyPhrase, parameters);
    }

    public static String get(int guildId, String keyPhrase, Object... parameters) {
        if (!Config.SHOW_KEYPHRASE) {
            return String.format(get(guildId, keyPhrase), parameters);
        }
        return "`" + keyPhrase + "` params: `" + Joiner.on("`, `").join(parameters) + "`";
    }

    /**
     * refreshes the data from the database
     */
    public static synchronized void initialize() {
        dictionary.clear();
        guildDictionary.clear();
        try (ResultSet rs = WebDb.get().select("SELECT id,guild_id, keyphrase, text FROM template_texts")) {
            while (rs.next()) {
                String keyphrase = rs.getString("keyphrase");
                int guildId = rs.getInt("guild_id");
                if (guildId == 0) {
                    if (!dictionary.containsKey(keyphrase)) {
                        dictionary.put(keyphrase, new ArrayList<>());
                    }
                    dictionary.get(keyphrase).add(rs.getString("text"));
                } else {
                    if (!guildDictionary.containsKey(guildId)) {
                        guildDictionary.put(guildId, new ConcurrentHashMap<>());
                    }
                    if (!guildDictionary.get(guildId).containsKey(keyphrase)) {
                        guildDictionary.get(guildId).put(keyphrase, new ArrayList<>());
                    }
                    guildDictionary.get(guildId).get(keyphrase).add(rs.getString("text"));
                }
            }
            rs.getStatement().close();
        } catch (SQLException e) {
            e.getStackTrace();
        }
    }

    public static synchronized void initialize(int guildId) {
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

    /**
     * Refresh only a keyphase
     *
     * @param keyPhrase phrase to refresh
     */
    public synchronized void reload(String keyPhrase) {
        if (!dictionary.containsKey(keyPhrase)) {
            dictionary.put(keyPhrase, new ArrayList<>());
        }
        dictionary.get(keyPhrase).clear();
        try (ResultSet rs = WebDb.get().select("SELECT text FROM template_texts WHERE keyphrase = ? AND guild_id = 0", keyPhrase)) {
            dictionary.get(keyPhrase).add(rs.getString("text"));
            rs.getStatement().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}