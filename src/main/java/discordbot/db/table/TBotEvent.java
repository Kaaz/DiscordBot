package discordbot.db.table;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OBotEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * data communication with the table `bot_events`
 */
public class TBotEvent {

	public static OBotEvent findBy(String id) {
		OBotEvent s = new OBotEvent();
		try (ResultSet rs = WebDb.get().select(
				"SELECT id, created_on, event_group, sub_group, data " +
						"FROM bot_events " +
						"WHERE id = ? ", id)) {
			if (rs.next()) {
				s = fillRecord(rs);
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return s;
	}
	private static OBotEvent fillRecord(ResultSet rs) throws SQLException {
		OBotEvent s = new OBotEvent();
		s.id = rs.getInt("id");
		s.createdOn = rs.getTimestamp("created_on");
		s.group = rs.getString("event_group");
		s.subGroup = rs.getString("sub_group");
		s.data = rs.getString("data");
		return s;
	}

	public static List<OBotEvent> getEventsAfter(int id) {
		List<OBotEvent> list = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select("SELECT * FROM bot_events WHERE id > ? ", id)) {
			while (rs.next()) {
				list.add(fillRecord(rs));
			}
			rs.getStatement().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static void insert(String group, String subGroup, String data) {
		OBotEvent oBotEvent = new OBotEvent();
		oBotEvent.group = group;
		oBotEvent.subGroup = subGroup;
		oBotEvent.data = data;
		insert(oBotEvent);
	}

	public static void insert(OBotEvent record) {
		try {
			record.id = WebDb.get().insert(
					"INSERT INTO bot_events(created_on, event_group, sub_group, data) " +
							"VALUES (?,?,?,?)",
					new Timestamp(System.currentTimeMillis()), record.group, record.subGroup, record.data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
