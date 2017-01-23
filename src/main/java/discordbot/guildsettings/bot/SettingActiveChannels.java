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


public class SettingActiveChannels extends AbstractGuildSetting<EnumSettingType> {

	@Override
	protected EnumSettingType getSettingsType() {
		return new EnumSettingType("mine", "all");
	}

	@Override
	public String getKey() {
		return "bot_listen";
	}

	@Override
	public String[] getTags() {
		return new String[]{"bot","listen"};
	}

	@Override
	public String getDefault() {
		return "all";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"What channels to listen to? (all;mine)",
				"all -> responds to all channels",
				"mine -> only responds to messages in configured channel"};
	}
}
