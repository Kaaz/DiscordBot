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
import emily.db.model.OReactionRoleKey;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CReactionRoleKey {

    public static OReactionRoleKey findBy(String discordGuildId, String key) {
        return findBy(CGuild.getCachedId(discordGuildId), key);
    }

    public static OReactionRoleKey findOrCreate(String discordGuildid, String key) {
        OReactionRoleKey rec = findBy(CGuild.getCachedId(discordGuildid), key);
        if (rec.id == 0) {
            rec.guildId = CGuild.getCachedId(discordGuildid);
            rec.messageKey = key;
            insert(rec);
        }
        return rec;
    }

    public static OReactionRoleKey findBy(int serverId, String key) {
        OReactionRoleKey t = new OReactionRoleKey();
        try (ResultSet rs = WebDb.get().select(
                "SELECT *  " +
                        "FROM reaction_role_key " +
                        "WHERE guild_id = ? AND message_key = ? ", serverId, key)) {
            if (rs.next()) {
                t = fillRecord(rs);
            }
            rs.getStatement().close();
        } catch (Exception e) {
            Logger.fatal(e);
        }
        return t;
    }

    public static List<OReactionRoleKey> getKeysForGuild(String guildDiscordId) {
        return getKeysForGuild(CGuild.getCachedId(guildDiscordId));
    }

    public static List<OReactionRoleKey> getKeysForGuild(int guildId) {
        List<OReactionRoleKey> result = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select(
                "SELECT *  " +
                        "FROM reaction_role_key " +
                        "WHERE guild_id = ?", guildId)) {
            while (rs.next()) {
                result.add(fillRecord(rs));
            }
            rs.getStatement().close();
        } catch (Exception e) {
            Logger.fatal(e);
        }
        return result;

    }

    private static OReactionRoleKey fillRecord(ResultSet rs) throws SQLException {
        OReactionRoleKey t = new OReactionRoleKey();
        t.id = rs.getInt("id");
        t.guildId = rs.getInt("guild_id");
        t.messageKey = rs.getString("message_key");
        t.channelId = rs.getLong("channel_id");
        t.message = rs.getString("message");
        t.messageId = rs.getLong("message_id");
        return t;
    }

    public static void delete(OReactionRoleKey record) {
        try {
            WebDb.get().query(
                    "DELETE FROM reaction_role_key WHERE message_key = ? AND guild_id = ? ",
                    record.messageKey, record.guildId
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteGuild(String guildId) {
        deleteGuild(CGuild.getCachedId(guildId));
    }

    public static void deleteGuild(int guildId) {
        try {
            WebDb.get().query(
                    "DELETE FROM reaction_role_key WHERE guild_id = ? ",
                    guildId
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void update(OReactionRoleKey record) {
        try {
            WebDb.get().query(
                    "UPDATE reaction_role_key SET message_key = ?, channel_id = ?, message = ?, message_id = ? WHERE id = ?",
                    record.messageKey, record.channelId, record.message, record.messageId, record.id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insert(OReactionRoleKey record) {
        if (record.id > 0) {
            update(record);
            return;
        }
        try {
            record.id = WebDb.get().insert(
                    "INSERT INTO reaction_role_key(guild_id, message_key, channel_id, message, message_id) " +
                            "VALUES (?,?,?,?,?)",
                    record.guildId, record.messageKey, record.channelId, record.message, record.messageId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
