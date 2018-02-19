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

package emily.db.controllers;

import emily.core.Logger;
import emily.db.WebDb;
import emily.db.model.OUser;
import emoji4j.EmojiUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * data communication with the controllers `users`
 * Created on 10-8-2016
 */
public class CUser {

    private static Map<Long, Integer> userCache = new ConcurrentHashMap<>();
    private static Map<Integer, Long> discordCache = new ConcurrentHashMap<>();

    public static int getCachedId(long discordId) {
        return getCachedId(discordId, String.valueOf(discordId));
    }

    public static int getCachedId(long discordId, String username) {
        if (!userCache.containsKey(discordId)) {
            OUser user = findBy(discordId);
            if (user.id == 0) {
                user.discord_id = String.valueOf(discordId);
                user.name = username;
                insert(user);
            }
            if (user.name == null || user.name.isEmpty() || user.name.equals(username)) {
                user.name = EmojiUtils.shortCodify(username);
                update(user);
            }
            userCache.put(discordId, user.id);
        }
        return userCache.get(discordId);
    }

    public static long getCachedDiscordId(int userId) {
        if (!discordCache.containsKey(userId)) {
            OUser user = findById(userId);
            if (user.id == 0) {
                return 0L;
            }
            discordCache.put(userId, Long.parseLong(user.discord_id));
        }
        return discordCache.get(userId);
    }


    public static OUser findBy(long discordId) {
        OUser s = new OUser();
        try (ResultSet rs = WebDb.get().select(
                "SELECT *  " +
                        "FROM users " +
                        "WHERE discord_id = ? ", discordId)) {
            if (rs.next()) {
                s = fillRecord(rs);
            }
            rs.getStatement().close();
        } catch (Exception e) {
            Logger.fatal(e);
        }
        return s;
    }

    public static OUser findById(int internalId) {
        OUser s = new OUser();
        try (ResultSet rs = WebDb.get().select(
                "SELECT *  " +
                        "FROM users " +
                        "WHERE id = ? ", internalId)) {
            if (rs.next()) {
                s = fillRecord(rs);
            }
            rs.getStatement().close();
        } catch (Exception e) {
            Logger.fatal(e);
        }
        return s;
    }

    private static OUser fillRecord(ResultSet rs) throws SQLException {
        OUser s = new OUser();
        s.id = rs.getInt("id");
        s.discord_id = rs.getString("discord_id");
        s.name = rs.getString("name");
        s.commandsUsed = rs.getInt("commands_used");
        s.banned = rs.getInt("banned");
        s.setPermission(rs.getInt("permission_mask"));
        s.lastCurrencyRetrieval = rs.getInt("last_currency_retrieval");
        return s;
    }

    public static List<OUser> getBannedUsers() {
        List<OUser> list = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select("SELECT * FROM users WHERE banned = 1")) {
            while (rs.next()) {
                list.add(fillRecord(rs));
            }
            rs.getStatement().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void registerCommandUse(int userId) {
        try {
            WebDb.get().query(
                    "UPDATE users SET  commands_used = commands_used + 1 " +
                            "WHERE id = ? ",
                    userId
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void update(OUser record) {
        if (record.id == 0) {
            insert(record);
            return;
        }
        try {
            WebDb.get().query(
                    "UPDATE users SET discord_id = ?, name = ?, banned = ?, commands_used = ?, permission_mask = ?, last_currency_retrieval = ? " +
                            "WHERE id = ? ",
                    record.discord_id, record.name, record.banned, record.commandsUsed, record.getEncodedPermissions(), record.lastCurrencyRetrieval, record.id
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insert(OUser record) {
        try {
            record.id = WebDb.get().insert(
                    "INSERT INTO users(discord_id,commands_used, name,banned, permission_mask, last_currency_retrieval) " +
                            "VALUES (?,?,?,?,?,?)",
                    record.discord_id, record.commandsUsed, record.name, record.banned, record.getEncodedPermissions(), System.currentTimeMillis() / 1000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addBannedUserIds(HashSet<Long> bannedUsers) {
        try (ResultSet rs = WebDb.get().select("SELECT * FROM users WHERE banned = 1")) {
            while (rs.next()) {
                bannedUsers.add(rs.getLong("discord_id"));
            }
            rs.getStatement().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
