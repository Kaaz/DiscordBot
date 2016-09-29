package discordbot.db.table;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OTag;

import java.sql.ResultSet;

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
				t.id = rs.getInt("id");
				t.tagname = rs.getString("tag_name");
				t.guildId = rs.getInt("guild_id");
				t.response = rs.getString("response");
				t.userId = rs.getInt("user_id");
				t.created = rs.getTimestamp("creation_date");
			}
		} catch (Exception e) {
			Logger.fatal(e);
		}
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
