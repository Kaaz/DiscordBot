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

package discordbot.db.model;

import discordbot.db.AbstractModel;

import java.sql.Timestamp;

public class OBotEvent extends AbstractModel {
	public int id = 0;
	public Timestamp createdOn = null;
	public String group = "";
	public String subGroup = "";
	public String data = "";
	public Level logLevel = Level.INFO;

	public enum Level {
		FATAL(1),
		ERROR(2),
		WARN(3),
		INFO(4),
		DEBUG(5),
		TRACE(6);

		private final int id;

		Level(int id) {

			this.id = id;
		}

		public static Level fromId(int id) {
			for (Level et : values()) {
				if (id == et.getId()) {
					return et;
				}
			}
			return INFO;
		}

		public int getId() {
			return id;
		}
	}
}
