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

package discordbot.guildsettings.music;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.TextChannelSettingType;


public class SettingMusicChannel extends AbstractGuildSetting<TextChannelSettingType> {

	@Override
	protected TextChannelSettingType getSettingsType() {
		return new TextChannelSettingType(false);
	}

	@Override
	public String getKey() {
		return "music_channel";
	}

	@Override
	public String[] getTags() {
		return new String[]{"bot", "music", "channel"};
	}

	@Override
	public String getDefault() {
		return "music";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Channel where the bots music-related output goes to"};
	}
}