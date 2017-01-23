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
 * expanding on playlists
 */
public class db_25_to_26 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 25;
	}

	@Override
	public int getToVersion() {
		return 26;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"ALTER TABLE playlist ADD code VARCHAR(32) DEFAULT 'default' NOT NULL",
				"CREATE UNIQUE INDEX playlist_owner_id_guild_id_code_uindex ON playlist (owner_id, guild_id, code)",
				"CREATE INDEX playlist_owner_id_code_index ON playlist (owner_id, code)",
				"CREATE INDEX playlist_guild_id_code_index ON playlist (guild_id, code)"
		};
	}
}