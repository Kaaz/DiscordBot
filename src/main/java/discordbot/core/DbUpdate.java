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

package discordbot.core;

import discordbot.db.IDbVersion;
import discordbot.db.MySQLAdapter;
import org.reflections.Reflections;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DbUpdate {
	private final MySQLAdapter adapter;
	private int highestVersion = 0;
	private Map<Integer, IDbVersion> versionMap;

	public DbUpdate(MySQLAdapter adapter) {
		this.adapter = adapter;
		versionMap = new HashMap<>();
		collectDatabaseVersions();
	}

	private void collectDatabaseVersions() {
		Reflections reflections = new Reflections("discordbot.db.version");
		Set<Class<? extends IDbVersion>> classes = reflections.getSubTypesOf(IDbVersion.class);
		for (Class<? extends IDbVersion> s : classes) {
			try {
				IDbVersion iDbVersion = s.newInstance();
				highestVersion = Math.max(highestVersion, iDbVersion.getToVersion());
				versionMap.put(iDbVersion.getFromVersion(), iDbVersion);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean updateToCurrent() {
		int currentVersion = -1;
		try {
			currentVersion = getCurrentVersion();
			if (currentVersion == highestVersion) {
				return true;
			}
			boolean hasUpgrade = versionMap.containsKey(currentVersion);
			while (hasUpgrade) {
				IDbVersion dbVersion = versionMap.get(currentVersion);
				for (String query : dbVersion.getExecutes()) {
					System.out.println("EXECUTING::");
					System.out.println(query);
					adapter.insert(query);
				}
				currentVersion = dbVersion.getToVersion();
				saveDbVersion(currentVersion);
				hasUpgrade = versionMap.containsKey(currentVersion);
			}

		} catch (SQLException e) {
			System.out.println("Db version: " + currentVersion);
			e.printStackTrace();
		}
		return false;
	}

	private int getCurrentVersion() throws SQLException {
		DatabaseMetaData metaData = adapter.getConnection().getMetaData();
		int dbVersion = 0;
		try (ResultSet rs = metaData.getTables(null, null, "commands", null)) {
			if (!rs.next()) {
				return -1;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		try (ResultSet rs = metaData.getTables(null, null, "bot_meta", null)) {
			if (!rs.next()) {
				return dbVersion;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		try (ResultSet rs = adapter.select("SELECT * FROM bot_meta WHERE meta_name = ?", "db_version")) {
			if (rs.next()) {
				dbVersion = Integer.parseInt(rs.getString("meta_value"));
			}
			rs.getStatement().close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return dbVersion;
	}

	private void saveDbVersion(int version) throws SQLException {
		if (version < 1) {
			return;
		}
		adapter.insert("INSERT INTO bot_meta(meta_name, meta_value) VALUES (?,?) ON DUPLICATE KEY UPDATE meta_value = ? ", "db_version", version, version);
	}
}