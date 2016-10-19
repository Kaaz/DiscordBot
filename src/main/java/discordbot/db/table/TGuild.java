package discordbot.db.table;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OGuild;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * data communication with the table `servers`
 * Created on 10-8-2016
 */
public class TGuild {
	private static Map<String, Integer> guildIdCache = new ConcurrentHashMap<>();
	private static Map<Integer, String> discordIdCache = new ConcurrentHashMap<>();

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

	public static String getCachedDiscordId(int id) {
		if (!discordIdCache.containsKey(id)) {
			OGuild server = findById(id);
			if (server.id == 0) {
				return "";
			}
			discordIdCache.put(id, server.discord_id);
		}
		return discordIdCache.get(id);
	}

	public static OGuild findBy(String discordId) {
		OGuild s = new OGuild();
		try (ResultSet rs = WebDb.get().select(
				"SELECT id, discord_id, name, owner,active,banned  " +
						"FROM guilds " +
						"WHERE discord_id = ? ", discordId)) {
			if (rs.next()) {
				s = loadRecord(rs);
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return s;
	}

	public static OGuild findById(int id) {
		OGuild s = new OGuild();
		try (ResultSet rs = WebDb.get().select(
				"SELECT id, discord_id, name, owner,active,banned  " +
						"FROM guilds " +
						"WHERE id = ? ", id)) {
			if (rs.next()) {
				s = loadRecord(rs);
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
					"UPDATE guilds SET discord_id = ?, name = ?, owner = ?, active = ?, banned = ? " +
							"WHERE id = ? ",
					record.discord_id, record.name, record.owner, record.active, record.banned, record.id
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void insert(OGuild record) {
		try {
			record.id = WebDb.get().insert(
					"INSERT INTO guilds(discord_id, name, owner,active,banned) " +
							"VALUES (?,?,?,?,?)",
					record.discord_id, record.name, record.owner, record.active, record.banned);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * retrieves the amount of active guilds
	 * note: this value could be higher than the actual active guilds if the bot missed a leave guild event
	 *
	 * @return active guild count
	 */
	public static int getActiveGuildCount() {
		int amount = 0;
		try (ResultSet rs = WebDb.get().select("SELECT sum(id) AS amount FROM guilds WHERE active = 1")) {
			while (rs.next()) {
				amount = rs.getInt("amount");
			}
			rs.getStatement().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return amount;
	}

	public static List<OGuild> getBannedGuilds() {
		List<OGuild> list = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select("SELECT * FROM guilds WHERE banned = 1")) {
			while (rs.next()) {
				list.add(loadRecord(rs));
			}
			rs.getStatement().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	private static OGuild loadRecord(ResultSet rs) throws SQLException {
		OGuild s = new OGuild();
		s.id = rs.getInt("id");
		s.discord_id = rs.getString("discord_id");
		s.name = rs.getString("name");
		s.owner = rs.getInt("owner");
		s.active = rs.getInt("active");
		s.banned = rs.getInt("banned");
		return s;
	}
}
