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
 * the ability for users to rate songs
 */
public class db_08_to_09 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 8;
	}

	@Override
	public int getToVersion() {
		return 9;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				" CREATE TABLE music_votes " +
						" (	song_id INT NOT NULL, " +
						"		user_id INT NOT NULL," +
						"		vote INT NOT NULL," +
						"		created_on TIMESTAMP NOT NULL," +
						"		CONSTRAINT music_votes_song_id_user_id_pk PRIMARY KEY (song_id, user_id) )"
		};
	}
}