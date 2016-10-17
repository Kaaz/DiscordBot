package discordbot.db.table;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OUser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * data communication with the table `users`
 * Created on 10-8-2016
 */
public class TUser {

	private static Map<String, Integer> userCache = new ConcurrentHashMap<>();

	public static int getCachedId(String discordId) {
		if (!userCache.containsKey(discordId)) {
			OUser user = findBy(discordId);
			if (user.id == 0) {
				user.discord_id = discordId;
				insert(user);
			}
			userCache.put(discordId, user.id);
		}
		return userCache.get(discordId);
	}

	public static OUser findBy(String discordId) {
		OUser s = new OUser();
		try (ResultSet rs = WebDb.get().select(
				"SELECT *  " +
						"FROM users " +
						"WHERE discord_id = ? ", discordId)) {
			if (rs.next()) {
				s = fillRecord(rs);
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return s;
	}

	public static OUser findById(int internalId) {
		OUser s = new OUser();
		try (ResultSet rs = WebDb.get().select(
				"SELECT *  " +
						"FROM users " +
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

	private static OUser fillRecord(ResultSet rs) throws SQLException {
		OUser s = new OUser();
		s.id = rs.getInt("id");
		s.discord_id = rs.getString("discord_id");
		s.name = rs.getString("name");
		s.banned = rs.getInt("banned");
		return s;
	}

	public static List<OUser> getBannedUsers() {
		List<OUser> list = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select("SELECT * FROM users WHERE banned = 1")) {
			while (rs.next()) {
				list.add(fillRecord(rs));
			}
			rs.getStatement().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static void update(OUser record) {
		if (record.id == 0) {
			insert(record);
			return;
		}
		try {
			WebDb.get().query(
					"UPDATE users SET discord_id = ?, name = ?, banned = ?" +
							"WHERE id = ? ",
					record.discord_id, record.name, record.banned, record.id
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void insert(OUser record) {
		try {
			record.id = WebDb.get().insert(
					"INSERT INTO users(discord_id, name,banned) " +
							"VALUES (?,?,?)",
					record.discord_id, record.name, record.banned);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
