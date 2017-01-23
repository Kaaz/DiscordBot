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
import discordbot.db.model.OBotVersionChange;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * data communication with the controllers `bot_versions`
 */
public class CBotVersionChanges {

	public static OBotVersionChange findById(int id) {
		OBotVersionChange s = new OBotVersionChange();
		try (ResultSet rs = WebDb.get().select(
				"SELECT * " +
						"FROM bot_version_changes " +
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

	/**
	 * retrieve all changes for specified version
	 *
	 * @param versionId internal version id to look up
	 * @return list of changes
	 */
	public static List<OBotVersionChange> getChangesFor(int versionId) {
		List<OBotVersionChange> s = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select(
				"SELECT * " +
						"FROM bot_version_changes " +
						"WHERE version = ? ORDER BY change_type ASC ", versionId)) {
			while (rs.next()) {
				s.add(fillRecord(rs));
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return s;
	}

	private static OBotVersionChange fillRecord(ResultSet rs) throws SQLException {
		OBotVersionChange s = new OBotVersionChange();
		s.id = rs.getInt("id");
		s.version = rs.getInt("version");
		s.setChangeType(rs.getInt("change_type"));
		s.description = rs.getString("description");
		s.author = rs.getInt("author");
		return s;
	}

	public static int insert(int versionId, OBotVersionChange.ChangeType changeType, String description) {
		OBotVersionChange r = new OBotVersionChange();
		r.version = versionId;
		r.changeType = changeType;
		r.description = description;
		return insert(r);
	}

	public static int insert(OBotVersionChange record) {
		try {
			return WebDb.get().insert(
					"INSERT INTO bot_version_changes(version, change_type, description, author) " +
							"VALUES (?,?,?,?)",
					record.version, record.changeType.getId(), record.description, record.author);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
}