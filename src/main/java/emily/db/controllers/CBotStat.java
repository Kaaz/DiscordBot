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
import emily.db.model.OBotStats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * data communication with the controllers `bot_events`
 */
public class CBotStat {


    private static OBotStats fillRecord(ResultSet rs) throws SQLException {
        OBotStats s = new OBotStats();
        s.id = rs.getInt("id");
        s.createdOn = rs.getTimestamp("created_on");
        s.guildCount = rs.getLong("event_group");
        s.userCount = rs.getLong("sub_group");
        s.musicCount = rs.getLong("data");
        return s;
    }

    public static void insert(long guildCount, long userCount, long musicCount) {
        OBotStats stats = new OBotStats();
        stats.createdOn = new Timestamp(System.currentTimeMillis());
        stats.guildCount = guildCount;
        stats.userCount = userCount;
        stats.musicCount = musicCount;
        insert(stats);
    }


    public static void insert(OBotStats record) {
        try {
            record.id = WebDb.get().insert(
                    "INSERT INTO bot_stats(created_on,guild_count, user_count, music_count) " +
                            "VALUES (?,?,?,?)",
                    record.createdOn, record.guildCount, record.userCount, record.musicCount
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
