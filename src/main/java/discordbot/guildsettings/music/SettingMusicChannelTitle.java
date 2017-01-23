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
import discordbot.guildsettings.types.EnumSettingType;


public class SettingMusicChannelTitle extends AbstractGuildSetting<EnumSettingType> {
	@Override
	protected EnumSettingType getSettingsType() {
		return new EnumSettingType("auto", "true", "false");
	}

	@Override
	public String getKey() {
		return "music_channel_title";
	}

	@Override
	public String[] getTags() {
		return new String[]{"music", "channel", "title"};
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{
				"Updates the music channel's topic with the currently playing song",
				"",
				"auto  -> update the title every 10 seconds with the track its playing",
				"true  -> yes change the topic at the beginning of every song",
				"false -> leave the channel topic title alone!",
		};
	}
}