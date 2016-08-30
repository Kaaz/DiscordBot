package novaz.db.table;

import novaz.core.Logger;
import novaz.db.WebDb;
import novaz.db.model.OUser;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * data communication with the table `users`
 * Created on 10-8-2016
 */
public class TUser {

	private static Map<String, Integer> userCache = new HashMap<>();

	public static int getCachedId(String discordId) {
		if (!userCache.containsKey(discordId)) {
			OUser user = findBy(discordId);
			if (user.id == 0) {
				insert(user);
			}
			userCache.put(discordId, user.id);
		}
		return userCache.get(discordId);
	}

	public static OUser findBy(String discordId) {
		OUser s = new OUser();
		try (ResultSet rs = WebDb.get().select(
				"SELECT id, discord_id, name  " +
						"FROM users " +
						"WHERE discord_id = ? ", discordId)) {
			if (rs.next()) {
				s.id = rs.getInt("id");
				s.discord_id = rs.getString("discord_id");
				s.name = rs.getString("name");
			}
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return s;
	}

	public static void update(OUser record) {
		if (record.id == 0) {
			insert(record);
			return;
		}
		try {
			WebDb.get().query(
					"UPDATE users SET discord_id = ?, name = ? " +
							"WHERE id = ? ",
					record.discord_id, record.name, record.id
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void insert(OUser record) {
		try {
			record.id = WebDb.get().insert(
					"INSERT INTO users(discord_id, name) " +
							"VALUES (?,?)",
					record.discord_id, record.name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
