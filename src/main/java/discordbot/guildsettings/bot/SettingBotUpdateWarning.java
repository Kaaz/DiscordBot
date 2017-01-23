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

package discordbot.guildsettings.bot;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.EnumSettingType;


public class SettingBotUpdateWarning extends AbstractGuildSetting<EnumSettingType> {

	@Override
	protected EnumSettingType getSettingsType() {
		return new EnumSettingType("always", "playing", "off");
	}


	@Override
	public String getKey() {
		return "bot_update_warning";
	}

	@Override
	public String[] getTags() {
		return new String[]{"bot", "update", "warn"};
	}

	@Override
	public String getDefault() {
		return "playing";
	}

	@Override
	public String[] getDescription() {
		return new String[]{
				"Show a warning that there is an update and that the bot will be updating soon.",
				"always  -> always show the message in the bot's configured default channel",
				"playing -> only announce when the bot is playing music and in the bot's configured music channel",
				"off     -> don't announce when the bot is going down for an update"
		};
	}
}
