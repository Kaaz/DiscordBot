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
import emily.db.model.OMusicVote;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * data communication with the controllers `music_votes`
 */
public class CMusicVote {
    public static OMusicVote findBy(int songId, String userDiscordId) {
        return findBy(songId, CUser.getCachedId(userDiscordId));
    }

    public static OMusicVote findBy(int songId, int userId) {
        OMusicVote token = new OMusicVote();
        try (ResultSet rs = WebDb.get().select(
                "SELECT song_id, user_id, vote, created_on  " +
                        "FROM music_votes " +
                        "WHERE song_id = ? AND user_id = ? ", songId, userId)) {
            if (rs.next()) {
                token = fillRecord(rs);
            }
            rs.getStatement().close();
        } catch (Exception e) {
            Logger.fatal(e);
        }
        return token;
    }

    private static OMusicVote fillRecord(ResultSet resultset) throws SQLException {
        OMusicVote record = new OMusicVote();
        record.songId = resultset.getInt("song_id");
        record.userId = resultset.getInt("user_id");
        record.vote = resultset.getInt("vote");
        record.createdOn = resultset.getTimestamp("created_on");
        return record;
    }

    public static void insertOrUpdate(int songId, String userDiscordId, int vote) {
        insertOrUpdate(songId, CUser.getCachedId(userDiscordId), vote);
    }

    public static void insertOrUpdate(int songId, int userId, int vote) {
        try {
            WebDb.get().insert(
                    "INSERT INTO music_votes(song_id, user_id, vote, created_on) " +
                            "VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE  vote = ?",
                    songId, userId, vote, new Timestamp(System.currentTimeMillis()), vote);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
