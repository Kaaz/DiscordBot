package discordbot.db.controllers;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OBet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CBet {
	public static final int MAX_BET_AMOUNT = 1_000_000;

	public static List<OBet> findForGuild(int id) {
		List<OBet> ret = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select(
				"SELECT *  " +
						"FROM bets " +
						"WHERE guild_id = ? ", id)) {
			while (rs.next()) {
				ret.add(fillRecord(rs));
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return ret;
	}

	public static OBet findById(int id) {
		OBet t = new OBet();
		try (ResultSet rs = WebDb.get().select(
				"SELECT *  " +
						"FROM bets " +
						"WHERE id = ? ", id)) {
			if (rs.next()) {
				t = fillRecord(rs);
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return t;
	}


	private static OBet fillRecord(ResultSet rs) throws SQLException {
		OBet b = new OBet();
		b.id = rs.getInt("id");
		b.title = rs.getString("title");
		b.ownerId = rs.getInt("owner_id");
		b.guildId = rs.getInt("guild_id");
		b.createdOn = rs.getTimestamp("created_on");
		b.startedOn = rs.getTimestamp("started_on");
		b.endsAt = rs.getTimestamp("ends_at");
		b.price = rs.getInt("price");
		return b;
	}

	public static void delete(OBet record) {
		if (record.id == 0) {
			insert(record);
			return;
		}
		try {
			WebDb.get().query(
					"DELETE FROM bets WHERE id = ? ",
					record.id
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void update(OBet record) {
		try {
			record.id = WebDb.get().query(
					"UPDATE bets SET title = ?, started_on = ?, ends_at = ?, price = ? WHERE id = ?",
					record.title, record.startedOn, record.endsAt, record.price, record.id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void insert(OBet record) {
		if (record.id > 0) {
			update(record);
			return;
		}
		try {
			record.id = WebDb.get().insert(
					"INSERT INTO bets(title, owner_id, guild_id, created_on, started_on, ends_at, price) " +
							"VALUES (?,?,?,?,?,?,?)",
					record.title, record.ownerId, record.guildId, record.createdOn, record.startedOn, record.endsAt, record.price);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void createBet(String title, int price, int guildId, int userId) {
		OBet b = new OBet();
		b.title = title;
		b.price = price;
		b.guildId = guildId;
		b.ownerId = userId;
		b.createdOn = new Timestamp(System.currentTimeMillis());
		insert(b);
	}
}
