package novaz.db.table;

import novaz.core.Logger;
import novaz.db.WebDb;
import novaz.db.model.RServer;

import java.sql.ResultSet;

/**
 * Created on 10-8-2016
 */
public class TServers {
	public static RServer findBy(String discordId) {
		RServer s = new RServer();
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

	public static void update(RServer record) {
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

	public static void insert(RServer record) {
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
