package discordbot.db.controllers;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.ORank;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * data communication with the controllers `ranks`
 */
public class CRank {

	public static ORank findBy(String codeName) {
		ORank s = new ORank();
		try (ResultSet rs = WebDb.get().select(
				"SELECT id, code_name, full_name  " +
						"FROM ranks " +
						"WHERE code_name = ? ", codeName)) {
			if (rs.next()) {
				s = fillRecord(rs);
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return s;
	}

	public static ORank findById(int internalId) {
		ORank s = new ORank();
		try (ResultSet rs = WebDb.get().select(
				"SELECT id, code_name, full_name  " +
						"FROM ranks" +
						" WHERE id = ? ", internalId)) {
			if (rs.next()) {
				s = fillRecord(rs);
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return s;
	}

	private static ORank fillRecord(ResultSet rs) throws SQLException {
		ORank s = new ORank();
		s.id = rs.getInt("id");
		s.codeName = rs.getString("code_name");
		s.fullName = rs.getString("full_name");
		return s;
	}

	public static List<ORank> getRanks() {
		List<ORank> list = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select("SELECT id, code_name, full_name FROM ranks ")) {
			while (rs.next()) {
				list.add(fillRecord(rs));
			}
			rs.getStatement().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static void update(ORank record) {
		if (record.id == 0) {
			insert(record);
			return;
		}
		try {
			WebDb.get().query(
					"UPDATE ranks SET code_name = ?, full_name = ? " +
							"WHERE id = ? ",
					record.codeName, record.fullName, record.id
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void insert(ORank record) {
		try {
			record.id = WebDb.get().insert(
					"INSERT INTO ranks(code_name, full_name) " +
							"VALUES (?,?)",
					record.codeName, record.fullName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
