package discordbot.db.table;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OCommandCooldown;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * data communication with the table `service_variables`
 */
public class TCommandCooldown {

	public static OCommandCooldown findBy(String commandName, String targetId, int targetType) {
		OCommandCooldown record = new OCommandCooldown();
		try (ResultSet rs = WebDb.get().select(
				"SELECT command, target_id, target_type, last_time  " +
						"FROM command_cooldown " +
						"WHERE command = ? AND target_id = ? AND target_type = ?", commandName, targetId, targetType)) {
			if (rs.next()) {
				record = fillRecord(rs);
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return record;
	}

	private static OCommandCooldown fillRecord(ResultSet resultset) throws SQLException {
		OCommandCooldown record = new OCommandCooldown();
		record.command = resultset.getString("command");
		record.targetId = resultset.getString("target_id");
		record.targetType = resultset.getInt("target_type");
		record.lastTime = resultset.getLong("last_time");
		return record;
	}

	public static void insertOrUpdate(OCommandCooldown record) {
		try {
			WebDb.get().insert(
					"INSERT INTO command_cooldown(command, target_id, target_type, last_time) " +
							"VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE last_time = ?",
					record.command, record.targetId, record.targetType, record.lastTime, record.lastTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
