package discordbot.db.controllers;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OBotPlayingOn3;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * data communication with the controllers `bot_playing_on`
 */
public class CBotPlayingOn {


	private static OBotPlayingOn3 fillRecord(ResultSet resultset) throws SQLException {
		OBotPlayingOn3 record = new OBotPlayingOn3();
		record.guildId = resultset.getString("guild_id");
		record.channelId = resultset.getString("channel_id");
		return record;
	}

	public static void insert(String guildId, String channelId) {
		OBotPlayingOn3 rec = new OBotPlayingOn3();
		rec.guildId = guildId;
		rec.channelId = channelId;
		insert(rec);
	}

	public static List<OBotPlayingOn3> getAll() {
		List<OBotPlayingOn3> list = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select(
				"SELECT guild_id, channel_id  FROM bot_playing_on")) {
			while (rs.next()) {
				list.add(fillRecord(rs));
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return list;
	}

	public static void insert(OBotPlayingOn3 record) {
		try {
			WebDb.get().insert(
					"INSERT INTO bot_playing_on(guild_id, channel_id) " +
							"VALUES (?,?) ON DUPLICATE KEY UPDATE channel_id=channel_id",
					record.guildId, record.channelId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deleteAll() {
		try {
			WebDb.get().query("DELETE FROM bot_playing_on ");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
