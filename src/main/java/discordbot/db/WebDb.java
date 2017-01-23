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

package discordbot.db;

import discordbot.main.Config;

import java.sql.SQLException;
import java.util.HashMap;

public class WebDb {

	private static final String DEFAULT_CONNECTION = "discord";
	private static HashMap<String, MySQLAdapter> connections = new HashMap<>();

	public static MySQLAdapter get(String key) {
		if (connections.containsKey(key)) {
			return connections.get(key);
		}
		System.out.println(String.format("The MySQL connection '%s' is not set!", key));
		return null;
	}

	public static MySQLAdapter get() {
		return connections.get(DEFAULT_CONNECTION);
	}

	public static void init() {
		connections.clear();
		connections.put("discord", new MySQLAdapter(Config.DB_HOST, Config.DB_USER, Config.DB_PASS, Config.DB_NAME));
		try {
			get().query("SET NAMES utf8mb4");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			System.out.println("COULD NOT SET utf8mb4");
		}
	}
}