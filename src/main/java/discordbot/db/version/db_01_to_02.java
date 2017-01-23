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

package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * controllers for the tag command
 */
public class db_01_to_02 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 1;
	}

	@Override
	public int getToVersion() {
		return 2;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"CREATE TABLE tags\n" +
						"         (\n" +
						"         id INT PRIMARY KEY AUTO_INCREMENT,\n" +
						"         tag_name VARCHAR(32),\n" +
						"         guild_id INT,\n" +
						"         response TEXT\n" +
						"         )",
				"ALTER TABLE tags ADD user_id INT NULL",
				"ALTER TABLE tags ADD creation_date TIMESTAMP NULL"
		};
	}
}