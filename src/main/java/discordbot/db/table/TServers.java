package discordbot.db.table;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OServer;

import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * data communication with the table `servers`
 * Created on 10-8-2016
 */
public class TServers {
	private static Map<String, Integer> servercache = new ConcurrentHashMap<>();

	public static int getCachedId(String discordId) {
		if (!servercache.containsKey(discordId)) {
			OServer server = findBy(discordId);
			if (server.id == 0) {
				server.discord_id = discordId;
				insert(server);
			}
			servercache.put(discordId, server.id);
		}
		return servercache.get(discordId);
	}

	public static OServer findBy(String discordId) {
		OServer s = new OServer();
		try (ResultSet rs = WebDb.get().select(
				"SELECT id, discord_id, name, owner  " +
						"FROM servers " +
						"WHERE discord_id = ? ", discordId)) {
			if (rs.next()) {
				s.id = rs.getInt("id");
				s.discord_id = rs.getString("discord_id");
				s.name = rs.getString("name");
				s.owner = rs.getInt("owner");
			}
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return s;
	}

	public static void update(OServer record) {
		if (record.id == 0) {
			insert(record);
			return;
		}
		try {
			WebDb.get().query(
					"UPDATE servers SET discord_id = ?, name = ?, owner = ? " +
							"WHERE id = ? ",
					record.discord_id, record.name, record.owner, record.id
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void insert(OServer record) {
		try {
			record.id = WebDb.get().insert(
					"INSERT INTO servers(discord_id, name, owner) " +
							"VALUES (?,?,?)",
					record.discord_id, record.name, record.owner);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
