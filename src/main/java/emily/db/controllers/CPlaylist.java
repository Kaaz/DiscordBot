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
import emily.db.model.OMusic;
import emily.db.model.OPlaylist;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * data communication with the controllers `playlist`
 */
public class CPlaylist {
    private static Random rng = new Random();

    public static ArrayList<OPlaylist> getPlaylistsForUser(int userId) {
        ArrayList<OPlaylist> s = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select(
                "SELECT id,code, title, owner_id, guild_id, visibility_level, play_type, edit_type, create_date  " +
                        "FROM playlist " +
                        "WHERE owner_id = ? ", userId)) {
            while (rs.next()) {
                s.add(fillRecord(rs));
            }
            rs.getStatement().close();
        } catch (Exception e) {
            Logger.fatal(e);
        }
        return s;
    }

    public static ArrayList<OPlaylist> getPlaylistsForGuild(int guildId) {
        ArrayList<OPlaylist> s = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select(
                "SELECT id,code, title, owner_id, guild_id, visibility_level, play_type, edit_type, create_date  " +
                        "FROM playlist " +
                        "WHERE guild_id = ? ", guildId)) {
            while (rs.next()) {
                s.add(fillRecord(rs));
            }
            rs.getStatement().close();
        } catch (Exception e) {
            Logger.fatal(e);
        }
        return s;
    }

    public static OPlaylist getGlobalList() {
        OPlaylist globalList = findBy(0, 0, "default");
        if (globalList.id == 0) {
            globalList.title = "Global";
            globalList.code = "default";
            insert(globalList);
        }
        return globalList;
    }

    public static OPlaylist findBy(int userId, int guildId) {
        return findBy(userId, guildId, "default");
    }

    public static OPlaylist findBy(int userId, int guildId, String code) {
        OPlaylist s = new OPlaylist();
        try (ResultSet rs = WebDb.get().select(
                "SELECT *  " +
                        "FROM playlist " +
                        "WHERE owner_id = ? AND guild_id = ? AND code = ? ", userId, guildId, code)) {
            if (rs.next()) {
                s = fillRecord(rs);
            } else {
                s.ownerId = userId;
                s.guildId = guildId;
                s.code = code;
            }
            rs.getStatement().close();
        } catch (Exception e) {
            Logger.fatal(e);
        }
        return s;
    }

    public static OPlaylist findById(int internalId) {
        OPlaylist s = new OPlaylist();
        try (ResultSet rs = WebDb.get().select(
                "SELECT *  " +
                        "FROM playlist " +
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

    public static List<OMusic> getMusic(int playlistId, int maxListSize, int offset) {
        List<OMusic> ret = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select("" +
                "SELECT m.* " +
                "FROM music m " +
                "JOIN playlist_item pi ON pi.music_id = m.id " +
                "WHERE pi.playlist_id = ? " +
                "ORDER BY m.youtube_title ASC " +
                "LIMIT ?, ?", playlistId, offset, maxListSize)) {
            while (rs.next()) {
                ret.add(CMusic.fillRecord(rs));
            }
            rs.getStatement().close();
        } catch (SQLException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        return ret;
    }


    public static int getMusicCount(int playlistId) {
        int amount = 0;
        try (ResultSet rs = WebDb.get().select("SELECT count(*) AS amount FROM playlist_item WHERE playlist_id = ?", playlistId)) {
            while (rs.next()) {
                amount = rs.getInt("amount");
            }
            rs.getStatement().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return amount;
    }

    public static String getNextTrack(int playlistId, OPlaylist.PlayType playType) {
        switch (playType) {
            case LOOP:
                return getNextMusic(playlistId);
            case SHUFFLE:
            default:
                return getRandomMusic(playlistId);
        }
    }

    /**
     * Retrieves a somewhat random item from the playlist
     *
     * @param playlistId the playlist to look in
     * @return absolute path to file
     */
    public static String getRandomMusic(int playlistId) {
        List<String> potentialSongs = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select(
                "SELECT m.id, m.youtubecode " +
                        "FROM music m " +
                        "JOIN playlist_item pi ON pi.music_id = m.id " +
                        "JOIN playlist pl ON pl.id = pi.playlist_id  " +
                        "WHERE m.banned = 0 AND pl.id = ? " +
                        "ORDER BY pi.last_played ASC " +
                        "LIMIT 5", playlistId)) {
            while (rs.next()) {
                potentialSongs.add(rs.getString("youtubecode"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!potentialSongs.isEmpty()) {
            return potentialSongs.get(rng.nextInt(potentialSongs.size()));
        }
        return null;
    }

    public static String getNextMusic(int playlistId) {
        String filename = null;
        try (ResultSet rs = WebDb.get().select(
                "SELECT m.id, m.youtubecode " +
                        "FROM music m " +
                        "JOIN playlist_item pi ON pi.music_id = m.id " +
                        "JOIN playlist pl ON pl.id = pi.playlist_id  " +
                        "WHERE m.banned = 0 AND pl.id = ? " +
                        "ORDER BY pi.last_played ASC " +
                        "LIMIT 1", playlistId)) {
            while (rs.next()) {
                filename = rs.getString("youtubecode");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filename;
    }

    /**
     * Add a song to a playlist
     *
     * @param playlistId the playlist to add to
     * @param musicId    the id of the music record
     * @return success
     */
    public static boolean addToPlayList(int playlistId, int musicId) {
        try {
            WebDb.get().query(
                    "INSERT INTO playlist_item(playlist_id, music_id, last_played) " +
                            "VALUES (?,?,?) ON DUPLICATE KEY UPDATE last_played=last_played ",
                    playlistId, musicId, 0
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * checks if music is present in a playlist
     *
     * @param playlistId the playlist to check
     * @param musicId    id of the music record
     * @return found music in list?
     */
    public static boolean isInPlaylist(int playlistId, int musicId) {
        boolean isInList = false;
        try (ResultSet rs = WebDb.get().select(
                "SELECT * FROM playlist_item WHERE playlist_id = ? AND music_id = ?",
                playlistId, musicId)) {
            if (rs.next()) {
                isInList = true;
            }
            rs.getStatement().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isInList;
    }

    /**
     * empty the whole playlist!
     *
     * @param playlistId id of the playlist
     */
    public static void resetPlaylist(int playlistId) {
        try {
            WebDb.get().query(
                    "DELETE FROM playlist_item WHERE playlist_id = ?",
                    playlistId
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * delete a track from the playlist
     *
     * @param playlistId internal playlist id
     * @param musicId    internal music id
     * @return track removed?
     */
    public static boolean removeFromPlayList(int playlistId, int musicId) {
        try {
            WebDb.get().query(
                    "DELETE FROM playlist_item WHERE playlist_id = ? AND music_id = ?",
                    playlistId, musicId
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * updates the last time a song was played in a playlist
     *
     * @param playlistId id of the playlist
     * @param musicId    id of the music recordf
     */
    public static void updateLastPlayed(int playlistId, int musicId) {
        try {
            WebDb.get().query("UPDATE playlist_item SET last_played = ? WHERE playlist_id = ? AND music_id = ?",
                    System.currentTimeMillis() / 1000L, playlistId, musicId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static OPlaylist fillRecord(ResultSet rs) throws SQLException {
        OPlaylist r = new OPlaylist();
        r.id = rs.getInt("id");
        r.title = rs.getString("title");
        r.ownerId = rs.getInt("owner_id");
        r.guildId = rs.getInt("guild_id");
        r.setCode(rs.getString("code"));
        r.setEditType(rs.getInt("edit_type"));
        r.setPlayType(rs.getInt("play_type"));
        r.setVisibility(rs.getInt("visibility_level"));
        r.createdOn = rs.getTimestamp("create_date");
        return r;
    }

    public static void update(OPlaylist record) {
        if (record.id == 0) {
            insert(record);
            return;
        }
        if (record.hasCodeChanged()) {

        }
        try {
            WebDb.get().query(
                    "UPDATE playlist SET title = ?, owner_id = ?, guild_id = ?, visibility_level = ?, edit_type = ?, play_type = ?, code = ? " +
                            "WHERE id = ? ",
                    record.title, record.ownerId, record.guildId, record.getVisibility().getId(), record.getEditType().getId(),
                    record.getPlayType().getId(), record.code, record.id
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insert(OPlaylist record) {
        try {
            record.createdOn = new Timestamp(System.currentTimeMillis());
            record.id = WebDb.get().insert(
                    "INSERT INTO playlist(title, owner_id, guild_id, visibility_level, edit_type,play_type, create_date,code) " +
                            "VALUES (?,?,?,?,?,?,?,?)",
                    record.title, record.ownerId, record.guildId, record.getVisibility().getId(), record.getEditType().getId(),
                    record.getPlayType().getId(), record.createdOn, record.code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * removes a track from all playlists
     *
     * @param musicId internal music id
     */
    public static void deleteTrackFromPlaylists(int musicId) {
        try {
            WebDb.get().query(
                    "DELETE FROM playlist_item WHERE music_id = ?",
                    musicId
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}