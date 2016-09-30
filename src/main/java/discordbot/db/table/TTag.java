package discordbot.db.table;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OTag;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TTag {

	public static OTag findBy(String discordGuildId, String tagname) {
		return findBy(TServers.getCachedId(discordGuildId), tagname);
	}

	public static OTag findBy(int serverId, String tagName) {
		OTag t = new OTag();
		try (ResultSet rs = WebDb.get().select(
				"SELECT *  " +
						"FROM tags " +
						"WHERE guild_id = ? AND tag_name = ? ", serverId, tagName)) {
			if (rs.next()) {
				t = fillRecord(rs);
			}
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return t;
	}

	public static List<OTag> getTagsFor(String guildDiscordId, String userDiscordId) {
		return getTagsFor(TServers.getCachedId(guildDiscordId), TUser.getCachedId(userDiscordId));
	}

	public static List<OTag> getTagsFor(int guildId, int userId) {
		List<OTag> result = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select(
				"SELECT *  " +
						"FROM tags " +
						"WHERE guild_id = ? AND user_id = ? ", guildId, userId)) {
			while (rs.next()) {
				result.add(fillRecord(rs));
			}
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return result;

	}

	public static List<OTag> getTagsFor(String guildDiscordId) {
		return getTagsFor(TServers.getCachedId(guildDiscordId));
	}

	public static List<OTag> getTagsFor(int guildId) {
		List<OTag> result = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select(
				"SELECT *  " +
						"FROM tags " +
						"WHERE guild_id = ? ", guildId)) {
			while (rs.next()) {
				result.add(fillRecord(rs));
			}
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return result;
	}

	private static OTag fillRecord(ResultSet rs) throws SQLException {
		OTag t = new OTag();
		t.id = rs.getInt("id");
		t.tagname = rs.getString("tag_name");
		t.guildId = rs.getInt("guild_id");
		t.response = rs.getString("response");
		t.userId = rs.getInt("user_id");
		t.created = rs.getTimestamp("creation_date");
		return t;
	}

	public static void delete(OTag record) {
		if (record.id == 0) {
			insert(record);
			return;
		}
		try {
			WebDb.get().query(
					"DELETE FROM tags WHERE tag_name = ? and guild_id = ? ",
					record.tagname, record.guildId
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void update(OTag record) {
		try {
			record.id = WebDb.get().query(
					"UPDATE tags SET response = ? WHERE id = ?",
					record.response, record.id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void insert(OTag record) {
		if (record.id > 0) {
			update(record);
			return;
		}
		try {
			record.id = WebDb.get().insert(
					"INSERT INTO tags(tag_name, guild_id, response, user_id, creation_date) " +
							"VALUES (?,?,?,?,?)",
					record.tagname, record.guildId, record.response, record.userId, record.created);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
