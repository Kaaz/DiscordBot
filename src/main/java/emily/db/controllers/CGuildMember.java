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
import emily.db.model.OGuildMember;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * data communication with the controllers `guild_member`
 */
public class CGuildMember {

    public static OGuildMember findBy(long guildDiscordId, long userDiscordId) {
        return findBy(CGuild.getCachedId(guildDiscordId), CUser.getCachedId(userDiscordId));
    }

    public static OGuildMember findBy(int guildId, int userId) {
        OGuildMember record = new OGuildMember();
        try (ResultSet rs = WebDb.get().select(
                "SELECT *  " +
                        "FROM guild_member " +
                        "WHERE guild_id = ? AND user_id = ? ", guildId, userId)) {
            if (rs.next()) {
                record = fillRecord(rs);
            } else {
                record.guildId = guildId;
                record.userId = userId;
            }
            rs.getStatement().close();
        } catch (Exception e) {
            Logger.fatal(e);
        }
        return record;
    }

    private static OGuildMember fillRecord(ResultSet resultset) throws SQLException {
        OGuildMember record = new OGuildMember();
        record.guildId = resultset.getInt("guild_id");
        record.userId = resultset.getInt("user_id");
        record.joinDate = resultset.getTimestamp("join_date");
        return record;
    }

    public static void insertOrUpdate(OGuildMember record) {
        try {
            WebDb.get().insert(
                    "INSERT INTO guild_member(guild_id, user_id, join_date) " +
                            "VALUES (?,?,?) ON DUPLICATE KEY UPDATE join_date = ?",
                    record.guildId, record.userId, record.joinDate, record.joinDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
