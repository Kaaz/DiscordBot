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

import emily.db.WebDb;
import emily.db.model.OBlacklistCommand;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * data communication with the controllers `music_votes`
 */
public class CBlacklistCommand {


    private static OBlacklistCommand fillRecord(ResultSet resultset) throws SQLException {
        OBlacklistCommand record = new OBlacklistCommand();
        record.guildId = resultset.getInt("guild_id");
        record.command = resultset.getString("command");
        record.channelId = resultset.getString("channel_id");
        record.blacklisted = 1 == resultset.getInt("enabled");
        return record;
    }

    /**
     * checks if a command is blacklisted, returns null if its not blacklisted
     *
     * @param guildId   the guild to check it for
     * @param command   the command to check
     * @param channelId the command to check
     * @return OBlacklistCommand || null
     */
    public static OBlacklistCommand find(int guildId, String command, String channelId) {
        OBlacklistCommand ret = null;
        try (ResultSet rs = WebDb.get().select("SELECT * FROM blacklist_commands WHERE guild_id = ? AND command = ? AND channel_id = ? ORDER BY command,channel_id", guildId, command, channelId)) {
            while (rs.next()) {
                ret = fillRecord(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static OBlacklistCommand find(int guildId, String command) {
        OBlacklistCommand ret = null;
        try (ResultSet rs = WebDb.get().select("SELECT * FROM blacklist_commands WHERE guild_id = ? AND command = ? ORDER BY command,channel_id", guildId, command)) {
            while (rs.next()) {
                ret = fillRecord(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * retrieves a list of ALL blacklisted items
     *
     * @return list
     */
    public static List<OBlacklistCommand> getAllBlacklisted() {
        List<OBlacklistCommand> ret = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select("SELECT * FROM blacklist_commands")) {
            while (rs.next()) {
                ret.add(fillRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Retrieves a list of blacklisted commands for a guild
     *
     * @param guildId internal guild id
     * @return list
     */
    public static List<OBlacklistCommand> getBlacklistedFor(int guildId) {
        List<OBlacklistCommand> ret = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select("SELECT * FROM blacklist_commands WHERE guild_id = ?", guildId)) {
            while (rs.next()) {
                ret.add(fillRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Delete all items for a guild/command combination
     *
     * @param guildId internal guild id
     * @param command command name
     */
    public static void deleteCommandOverrides(int guildId, String command) {
        try {
            WebDb.get().query("DELETE FROM blacklist_commands WHERE guild_id = ? AND command = ?", guildId, command);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteOverridesInChannel(int guildId, String channelId) {
        try {
            WebDb.get().query("DELETE FROM blacklist_commands WHERE guild_id = ? AND channel_id = ?", guildId, channelId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAllOverrides(int guildId) {
        try {
            WebDb.get().query("DELETE FROM blacklist_commands WHERE guild_id = ? AND channel_id NOT LIKE '0'", guildId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteGuild(int guildId) {
        try {
            WebDb.get().query("DELETE FROM blacklist_commands WHERE guild_id = ? ", guildId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete a blacklist item for a channel
     *
     * @param guildId internal guild id
     * @param command command name
     * @param channel discord channel id
     */
    public static void delete(int guildId, String command, String channel) {
        try {
            WebDb.get().query("DELETE FROM blacklist_commands WHERE guild_id = ? AND command = ? AND channel_id = ?", guildId, command, channel);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Insert/updates a guild-wide command blacklist
     *
     * @param discordGuildId discord guild id
     * @param command        command name
     */
    public static void insertOrUpdate(String discordGuildId, String command) {
        insertOrUpdate(CGuild.getCachedId(discordGuildId), command, "0", true);
    }

    /**
     * Insert/updates a channel command blacklist override
     *
     * @param discordGuildId discord guild id
     * @param command        command name
     * @param channel        discord channel id
     */
    public static void insertOrUpdate(String discordGuildId, String command, String channel) {
        insertOrUpdate(CGuild.getCachedId(discordGuildId), command, channel, true);
    }

    /**
     * Insert/updates a channel command blacklist override
     *
     * @param guildId discord guild id
     * @param command command name
     * @param channel discord channel id
     */
    public static void insertOrUpdate(int guildId, String command, String channel, boolean blacklisted) {
        try {
            WebDb.get().insert(
                    "INSERT INTO blacklist_commands(guild_id, command, channel_id,enabled) " +
                            "VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE  enabled = VALUES(enabled)",
                    guildId, command, channel, blacklisted ? 1 : 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
