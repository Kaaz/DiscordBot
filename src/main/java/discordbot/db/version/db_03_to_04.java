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
 * Track if a server is still active, mostly so it can send a different message the first time it connects to a guild
 */
public class db_03_to_04 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 3;
	}

	@Override
	public int getToVersion() {
		return 4;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"ALTER TABLE servers ADD active INT NULL"
		};
	}
}