package discordbot.db.table;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OBank;
import discordbot.main.Config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * data communication with the table `banks`
 */
public class TBanks {

	public static OBank findBy(String discordId) {
		return findBy(TUser.getCachedId(discordId));
	}

	public static OBank findBy(int userId) {
		OBank bank = new OBank();
		try (ResultSet rs = WebDb.get().select(
				"SELECT id, user, current_balance, created_on  " +
						"FROM banks " +
						"WHERE user = ? ", userId)) {
			if (rs.next()) {
				bank = fillRecord(rs);
			} else {
				bank.userId = userId;
				insert(bank);
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return bank;
	}

	private static OBank fillRecord(ResultSet resultset) throws SQLException {
		OBank bank = new OBank();
		bank.id = resultset.getInt("id");
		bank.userId = resultset.getInt("user");
		bank.currentBalance = resultset.getLong("current_balance");
		bank.createdOn = resultset.getTimestamp("created_on");
		return bank;
	}

	public static void insert(OBank bank) {
		if (bank.id > 0) {
			update(bank);
			return;
		}
		try {
			bank.currentBalance = Config.ECONOMY_START_BALANCE;
			bank.createdOn = new Timestamp(System.currentTimeMillis());
			bank.id = WebDb.get().insert(
					"INSERT INTO banks(user, current_balance, created_on) " +
							"VALUES (?,?,?)",
					bank.userId, bank.currentBalance, bank.createdOn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void update(OBank bank) {
		if (bank.id == 0) {
			insert(bank);
			return;
		}
		try {
			WebDb.get().insert(
					"UPDATE  banks SET current_balance = ? WHERE id = ? ",
					bank.currentBalance, bank.id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
