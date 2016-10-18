package discordbot.db.table;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OUserRank;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * data communication with the table `user_rank`
 */
public class TUserRank {

	public static OUserRank findBy(String userDiscordId) {
		return findBy(TUser.getCachedId(userDiscordId));
	}

	public static OUserRank findBy(int userId) {
		OUserRank record = new OUserRank();
		try (ResultSet rs = WebDb.get().select(
				"SELECT user_id, rank_type  " +
						"FROM user_rank " +
						"WHERE user_id = ? ", userId)) {
			if (rs.next()) {
				record = fillRecord(rs);
			} else {
				record.userId = userId;
				record.rankId = 0;
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return record;
	}

	public static List<OUserRank> getUsersWith(int rankId) {
		List<OUserRank> list = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select(
				"SELECT user_id, rank_type  " +
						"FROM user_rank " +
						"WHERE rank_type = ? ", rankId)) {
			while (rs.next()) {
				list.add(fillRecord(rs));
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return list;
	}

	private static OUserRank fillRecord(ResultSet resultset) throws SQLException {
		OUserRank record = new OUserRank();
		record.userId = resultset.getInt("user_id");
		record.rankId = resultset.getInt("rank_type");
		return record;
	}

	public static void insertOrUpdate(OUserRank record) {
		try {
			WebDb.get().insert(
					"INSERT INTO user_rank(user_id, rank_type) " +
							"VALUES (?,?) ON DUPLICATE KEY UPDATE rank_type = ?",
					record.userId, record.rankId, record.rankId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
