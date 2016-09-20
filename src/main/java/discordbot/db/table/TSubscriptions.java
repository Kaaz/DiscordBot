package discordbot.db.table;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OSubscription;
import discordbot.db.model.QActiveSubscriptions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * data communication with the table `subscriptions`
 */
public class TSubscriptions {
	public static OSubscription findBy(int serverId, int channelId, int serviceId) {
		OSubscription token = new OSubscription();
		try (ResultSet rs = WebDb.get().select(
				"SELECT *  " +
						"FROM subscriptions " +
						"WHERE server_id = ? AND channel_id = ? AND service_id = ? ", serverId, channelId, serviceId)) {
			if (rs.next()) {
				token = fillRecord(rs);
			}
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return token;
	}

	public static List<QActiveSubscriptions> getSubscriptionsForChannel(int channelId) {
		ArrayList<QActiveSubscriptions> list = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select("" +
				"SELECT se.id,se.name, se.display_name " +
				"FROM subscriptions su " +
				"JOIN services se ON se.id = su.service_id " +
				"WHERE su.channel_id = ? AND se.activated = 1 AND su.subscribed = 1 ", channelId)) {
			while (rs.next()) {
				QActiveSubscriptions row = new QActiveSubscriptions();
				row.serviceId = rs.getInt("id");
				row.code = rs.getString("name");
				row.displayName = rs.getString("display_name");
				list.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static List<QActiveSubscriptions> getSubscriptionsForService(int serviceId) {
		ArrayList<QActiveSubscriptions> list = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select("" +
				"SELECT se.id, su.channel_id, se.name,se.display_name  " +
				"FROM subscriptions su " +
				"JOIN services se ON se.id = su.service_id " +
				"WHERE se.id = ? AND su.subscribed = 1 ", serviceId)) {
			while (rs.next()) {
				QActiveSubscriptions row = new QActiveSubscriptions();
				row.serviceId = rs.getInt("id");
				row.channelId = rs.getInt("channel_id");
				row.code = rs.getString("name");
				row.displayName = rs.getString("display_name");
				list.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	private static OSubscription fillRecord(ResultSet resultset) throws SQLException {
		OSubscription record = new OSubscription();
		record.serverId = resultset.getInt("server_id");
		record.channelId = resultset.getInt("channel_id");
		record.serviceId = resultset.getInt("service_id");
		record.subscribed = resultset.getInt("subscribed");
		return record;
	}

	public static void insertOrUpdate(OSubscription record) {
		try {
			WebDb.get().insert(
					"INSERT INTO subscriptions(server_id, channel_id, service_id, subscribed) " +
							"VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE subscribed = ?",
					record.serverId, record.channelId, record.serviceId, record.subscribed, record.subscribed);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
