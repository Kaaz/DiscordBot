/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package discordbot.db.controllers;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OChannel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * data communication with the controllers `channels`
 * Created on 10-8-2016
 */
public class CChannels {
	private static Map<String, Integer> channelCache = new ConcurrentHashMap<>();

	public static int getCachedId(String discordId, String serverId) {
		return getCachedId(discordId, CGuild.getCachedId(serverId));
	}

	public static int getCachedId(String discordId, int serverId) {
		if (!channelCache.containsKey(discordId)) {
			OChannel channel = findBy(discordId);
			if (channel.id == 0) {
				channel.discord_id = discordId;
				channel.server_id = serverId;
				insert(channel);
			}
			channelCache.put(discordId, channel.id);
		}
		return channelCache.get(discordId);
	}

	public static OChannel findBy(String discordId) {
		OChannel s = new OChannel();
		try (ResultSet rs = WebDb.get().select(
				"SELECT id, discord_id, server_id, name " +
						"FROM channels " +
						"WHERE discord_id = ? ", discordId)) {
			if (rs.next()) {
				s.id = rs.getInt("id");
				s.server_id = rs.getInt("server_id");
				s.discord_id = rs.getString("discord_id");
				s.name = rs.getString("name");
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return s;
	}

	public static OChannel findById(int id) {
		OChannel s = new OChannel();
		try (ResultSet rs = WebDb.get().select(
				"SELECT id, discord_id, server_id, name " +
						"FROM channels " +
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

	private static OChannel fillRecord(ResultSet rs) throws SQLException {
		OChannel s = new OChannel();
		s.id = rs.getInt("id");
		s.server_id = rs.getInt("server_id");
		s.discord_id = rs.getString("discord_id");
		s.name = rs.getString("name");
		return s;
	}

	public static void update(OChannel record) {
		if (record.id == 0) {
			insert(record);
			return;
		}
		try {
			WebDb.get().query(
					"UPDATE channels SET discord_id = ?, server_id = ?, name = ? " +
							"WHERE id = ? ",
					record.discord_id, record.server_id, record.name, record.id
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void insert(OChannel record) {
		try {
			record.id = WebDb.get().insert(
					"INSERT INTO channels(discord_id, server_id, name) " +
							"VALUES (?,?,?)",
					record.discord_id, record.server_id, record.name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}