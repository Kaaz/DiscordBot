package novaz.db.table;

import novaz.db.WebDb;

import java.sql.Date;

/**
 * data communication with the table `command_log`
 * Created on 30-8-2016
 */
public class TCommandLog {

	public static void saveLog(int userId, int guildId, String commandUsed, String commandArgs) {
		try {
			WebDb.get().insert(
					"INSERT INTO command_log(user_id, guild, command, args, execute_date) " +
							"VALUES (?,?,?,?,?)",
					userId, guildId, commandUsed, commandArgs, new Date(System.currentTimeMillis()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}