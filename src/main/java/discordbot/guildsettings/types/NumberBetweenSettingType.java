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

package discordbot.guildsettings.types;

import discordbot.guildsettings.IGuildSettingType;
import net.dv8tion.jda.core.entities.Guild;

/**
 * number between settings type
 * the setting has to be between min, and max (including)
 */
public class NumberBetweenSettingType implements IGuildSettingType {
	private final int min, max;

	/**
	 * @param min minimum value
	 * @param max maximum value (including)
	 */
	public NumberBetweenSettingType(int min, int max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public String typeName() {
		return "enum";
	}

	@Override
	public boolean validate(Guild guild, String value) {
		try {
			int vol = Integer.parseInt(value);
			if (vol >= min && vol <= max) {
				return true;
			}
		} catch (Exception ignored) {
		}
		return false;
	}

	@Override
	public String fromInput(Guild guild, String value) {
		try {
			int vol = Integer.parseInt(value);
			if (vol >= min && vol <= max) {
				return "" + vol;
			}
		} catch (Exception ignored) {
		}
		return "";
	}

	@Override
	public String toDisplay(Guild guild, String value) {
		return value;
	}
}
