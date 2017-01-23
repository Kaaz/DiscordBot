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
import discordbot.db.model.OBotEvent;
import discordbot.main.Launcher;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * data communication with the controllers `bot_events`
 */
public class CBotEvent {

	public static OBotEvent findBy(String id) {
		OBotEvent s = new OBotEvent();
		try (ResultSet rs = WebDb.get().select(
				"SELECT id, created_on, event_group, log_level, sub_group, data " +
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
		s.logLevel = OBotEvent.Level.fromId(rs.getInt("log_level"));
		return s;
	}

	public static List<OBotEvent> getEventsAfter(int id) {
		List<OBotEvent> list = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select("SELECT * FROM bot_events WHERE id > ? ", id)) {
			while (rs.next()) {
				list.add(fillRecord(rs));
			}
			rs.getStatement().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public static void insert(String group, String subGroup, String data) {
		insert(OBotEvent.Level.INFO, group, subGroup, data);
	}

	public static void insert(OBotEvent.Level logLevel, String group, String subGroup, String data) {
		OBotEvent oBotEvent = new OBotEvent();
		oBotEvent.group = group;
		oBotEvent.subGroup = subGroup;
		oBotEvent.data = data;
		oBotEvent.logLevel = logLevel;
		insert(oBotEvent);
	}


	public static void insert(OBotEvent record) {
		try {
			record.id = WebDb.get().insert(
					"INSERT INTO bot_events(created_on, log_level, event_group, sub_group, data) " +
							"VALUES (?,?,?,?,?)",
					new Timestamp(System.currentTimeMillis()), record.logLevel.getId(), record.group, record.subGroup, record.data);
		} catch (Exception e) {
			e.printStackTrace();
			Launcher.logToDiscord(e, "data", record.data);
		}
	}
}
