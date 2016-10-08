package discordbot.db.table;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OGuild;

import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * data communication with the table `servers`
 * Created on 10-8-2016
 */
public class TGuild {
	private static Map<String, Integer> guildIdCache = new ConcurrentHashMap<>();

	public static int getCachedId(String discordId) {
		if (!guildIdCache.containsKey(discordId)) {
			OGuild server = findBy(discordId);
			if (server.id == 0) {
				server.discord_id = discordId;
				insert(server);
			}
			guildIdCache.put(discordId, server.id);
		}
		return guildIdCache.get(discordId);
	}

	public static OGuild findBy(String discordId) {
		OGuild s = new OGuild();
		try (ResultSet rs = WebDb.get().select(
				"SELECT id, discord_id, name, owner,active  " +
						"FROM servers " +
						"WHERE discord_id = ? ", discordId)) {
			if (rs.next()) {
				s.id = rs.getInt("id");
				s.discord_id = rs.getString("discord_id");
				s.name = rs.getString("name");
				s.owner = rs.getInt("owner");
				s.active = rs.getInt("active");
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return s;
	}

	public static void update(OGuild record) {
		if (record.id == 0) {
			insert(record);
			return;
		}
		try {
			WebDb.get().query(
					"UPDATE servers SET discord_id = ?, name = ?, owner = ?, active = ? " +
							"WHERE id = ? ",
					record.discord_id, record.name, record.owner, record.active, record.id
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void insert(OGuild record) {
		try {
			record.id = WebDb.get().insert(
					"INSERT INTO servers(discord_id, name, owner,active) " +
							"VALUES (?,?,?,?)",
					record.discord_id, record.name, record.owner, record.active);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
