package discordbot.db.table;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OReplyPattern;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * data communication with the table `reply_pattern`
 */
public class TReplyPattern {
	public static OReplyPattern findBy(String tag) {
		OReplyPattern record = new OReplyPattern();
		try (ResultSet rs = WebDb.get().select(
				"SELECT id, guild_id, user_id, tag, pattern, reply, created_on, cooldown  " +
						"FROM reply_pattern " +
						"WHERE tag = ? ", tag)) {
			if (rs.next()) {
				record = fillRecord(rs);
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return record;
	}

	private static OReplyPattern fillRecord(ResultSet rs) throws SQLException {
		OReplyPattern record = new OReplyPattern();
		record.id = rs.getInt("id");
		record.guildId = rs.getInt("guild_id");
		record.userId = rs.getInt("user_id");
		record.tag = rs.getString("tag");
		record.pattern = rs.getString("pattern");
		record.reply = rs.getString("reply");
		record.createdOn = rs.getTimestamp("created_on");
		record.cooldown = rs.getLong("cooldown");
		return record;
	}

	public static List<OReplyPattern> getAll() {
		List<OReplyPattern> list = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select(
				"SELECT id, guild_id, user_id, tag, pattern, reply, created_on, cooldown  " +
						"FROM reply_pattern")) {
			if (rs.next()) {
				list.add(fillRecord(rs));
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return list;
	}

	public static void insert(OReplyPattern r) {
		try {
			r.id = WebDb.get().insert(
					"INSERT INTO reply_pattern(guild_id, user_id, tag, pattern, reply, created_on, cooldown) " +
							"VALUES (?,?,?,?,?,?,?)",
					r.guildId, r.userId, r.tag, r.pattern, r.reply, new Timestamp(System.currentTimeMillis()), r.cooldown);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void update(OReplyPattern r) {
		try {
			r.id = WebDb.get().insert(
					"UPDATE reply_pattern SET tag = ?, pattern = ?, reply = ?, cooldown = ? " +
							"WHERE id = ? ",
					r.tag, r.pattern, r.reply, r.cooldown, r.id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
