package discordbot.db.controllers;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OPlaylist;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * data communication with the controllers `playlist`
 */
public class CPlaylist {

	public static OPlaylist findBy(int userId) {
		OPlaylist s = new OPlaylist();
		try (ResultSet rs = WebDb.get().select(
				"SELECT id, title, owner_id, guild_id, visibility_level, edit_type, create_date  " +
						"FROM playlist " +
						"WHERE owner_id = ? ", userId)) {
			if (rs.next()) {
				s = fillRecord(rs);
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return s;
	}

	public static OPlaylist findBy(int userId, int guildId) {
		OPlaylist s = new OPlaylist();
		try (ResultSet rs = WebDb.get().select(
				"SELECT id, title, owner_id, guild_id, visibility_level, edit_type, create_date  " +
						"FROM playlist " +
						"WHERE owner_id = ? AND guild_id = ?", userId, guildId)) {
			if (rs.next()) {
				s = fillRecord(rs);
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
				"SELECT id, title, owner_id, guild_id, visibility_level, edit_type, create_date  " +
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

	private static OPlaylist fillRecord(ResultSet rs) throws SQLException {
		OPlaylist r = new OPlaylist();
		r.id = rs.getInt("id");
		r.title = rs.getString("title");
		r.ownerId = rs.getInt("owner_id");
		r.guildId = rs.getInt("guild_id");
		r.setEditType(rs.getInt("edit_type"));
		r.setVisibility(rs.getInt("visibility_type"));
		r.createdOn = rs.getTimestamp("create_date");
		return r;
	}

	public static void update(OPlaylist record) {
		if (record.id == 0) {
			insert(record);
			return;
		}
		try {
			WebDb.get().query(
					"UPDATE playlist SET title = ?, owner_id = ?, guild_id = ?, visibility_level = ?, edit_type = ? " +
							"WHERE id = ? ",
					record.title, record.ownerId, record.guildId, record.getVisibility().getId(), record.getEditType().getId(), record.id
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void insert(OPlaylist record) {
		try {
			record.createdOn = new Timestamp(System.currentTimeMillis());
			record.id = WebDb.get().insert(
					"INSERT INTO playlist(title, owner_id, guild_id, visibility_level, edit_type, create_date) " +
							"VALUES (?,?,?,?,?,?)",
					record.title, record.ownerId, record.guildId, record.getVisibility().getId(), record.getEditType().getId(), record.createdOn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
