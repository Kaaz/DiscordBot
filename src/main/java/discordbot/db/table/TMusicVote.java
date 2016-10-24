package discordbot.db.table;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OMusicVote;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * data communication with the table `music_votes`
 */
public class TMusicVote {
	public static OMusicVote findBy(int songId, String userDiscordId) {
		return findBy(songId, TUser.getCachedId(userDiscordId));
	}

	public static OMusicVote findBy(int songId, int userId) {
		OMusicVote token = new OMusicVote();
		try (ResultSet rs = WebDb.get().select(
				"SELECT song_id, user_id, vote, created_on  " +
						"FROM music_votes " +
						"WHERE song_id = ? AND user_id = ? ", songId, userId)) {
			if (rs.next()) {
				token = fillRecord(rs);
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return token;
	}

	private static OMusicVote fillRecord(ResultSet resultset) throws SQLException {
		OMusicVote record = new OMusicVote();
		record.songId = resultset.getInt("song_id");
		record.userId = resultset.getInt("user_id");
		record.vote = resultset.getInt("vote");
		record.createdOn = resultset.getTimestamp("created_on");
		return record;
	}

	public static void insertOrUpdate(int songId, String userDiscordId, int vote) {
		insertOrUpdate(songId, TUser.getCachedId(userDiscordId), vote);
	}

	public static void insertOrUpdate(int songId, int userId, int vote) {
		try {
			WebDb.get().insert(
					"INSERT INTO music_votes(song_id, user_id, vote, created_on) " +
							"VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE  vote = ?",
					songId, userId, vote, new Timestamp(System.currentTimeMillis()), vote);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
