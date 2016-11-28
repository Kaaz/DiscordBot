package discordbot.db.controllers;

import discordbot.db.WebDb;
import discordbot.db.model.OBlacklistCommand;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * data communication with the controllers `music_votes`
 */
public class CBlacklistCommand {


	private static OBlacklistCommand fillRecord(ResultSet resultset) throws SQLException {
		OBlacklistCommand record = new OBlacklistCommand();
		record.guildId = resultset.getInt("guild_id");
		record.command = resultset.getString("command");
		return record;
	}

	/**
	 * checks if a command is blacklisted, returns null if its not blacklisted
	 *
	 * @param guildId the guild to check it for
	 * @param command the command to check
	 * @return OBlacklistCommand || null
	 */
	public static OBlacklistCommand find(int guildId, String command) {
		OBlacklistCommand ret = null;
		try (ResultSet rs = WebDb.get().select("SELECT * FROM blacklist_commands WHERE guild_id = ? AND command = ?", guildId, command)) {
			while (rs.next()) {
				ret = fillRecord(rs);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public static List<OBlacklistCommand> getAllBlacklisted() {
		List<OBlacklistCommand> ret = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select("SELECT * FROM blacklist_commands")) {
			while (rs.next()) {
				ret.add(fillRecord(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public static List<OBlacklistCommand> getBlacklistedFor(int guildId) {
		List<OBlacklistCommand> ret = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select("SELECT * FROM blacklist_commands WHERE guild_id = ?", guildId)) {
			while (rs.next()) {
				ret.add(fillRecord(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public static void delete(int guildId, String command) {
		try {
			WebDb.get().query("DELETE FROM blacklist_commands WHERE guild_id = ? AND command = ?", guildId, command);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void insertOrUpdate(String discordGuildId, String command) {
		insertOrUpdate(CGuild.getCachedId(discordGuildId), command);
	}

	public static void insertOrUpdate(int guildId, String command) {
		try {
			WebDb.get().insert(
					"INSERT INTO blacklist_commands(guild_id, command) " +
							"VALUES (?,?) ON DUPLICATE KEY UPDATE  guild_id = guild_id",
					guildId, command);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
