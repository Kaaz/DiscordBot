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
 * for music; Allow for a longer filename (up to 255)
 */
public class db_02_to_03 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 2;
	}

	@Override
	public int getToVersion() {
		return 3;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"ALTER TABLE playlist MODIFY filename VARCHAR(255) NOT NULL"
		};
	}
}