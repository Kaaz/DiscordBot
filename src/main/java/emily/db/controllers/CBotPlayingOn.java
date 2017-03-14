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
import emily.db.model.OBotPlayingOn;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * data communication with the controllers `bot_playing_on`
 */
public class CBotPlayingOn {


    private static OBotPlayingOn fillRecord(ResultSet resultset) throws SQLException {
        OBotPlayingOn record = new OBotPlayingOn();
        record.guildId = resultset.getString("guild_id");
        record.channelId = resultset.getString("channel_id");
        return record;
    }

    public static void insert(String guildId, String channelId) {
        OBotPlayingOn rec = new OBotPlayingOn();
        rec.guildId = guildId;
        rec.channelId = channelId;
        insert(rec);
    }

    public static List<OBotPlayingOn> getAll() {
        List<OBotPlayingOn> list = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select(
                "SELECT guild_id, channel_id  FROM bot_playing_on")) {
            while (rs.next()) {
                list.add(fillRecord(rs));
            }
            rs.getStatement().close();
        } catch (Exception e) {
            Logger.fatal(e);
        }
        return list;
    }

    public static void insert(OBotPlayingOn record) {
        try {
            WebDb.get().insert(
                    "INSERT INTO bot_playing_on(guild_id, channel_id) " +
                            "VALUES (?,?) ON DUPLICATE KEY UPDATE channel_id=channel_id",
                    record.guildId, record.channelId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteGuild(String guildId) {
        try {
            WebDb.get().query("DELETE FROM bot_playing_on WHERE guild_id = ?", guildId);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void deleteAll() {
        try {
            WebDb.get().query("DELETE FROM bot_playing_on ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
