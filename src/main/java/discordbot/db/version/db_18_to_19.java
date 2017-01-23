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
 * keep track of whether a file should exist or not
 */
public class db_18_to_19 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 18;
	}

	@Override
	public int getToVersion() {
		return 19;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"ALTER TABLE music ADD file_exists INT DEFAULT 1 NOT NULL"
		};
	}
}