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
import discordbot.guildsettings.types.NoSettingType;


public class SettingMusicLastPlaylist extends AbstractGuildSetting<NoSettingType> {
	@Override
	protected NoSettingType getSettingsType() {
		return new NoSettingType();
	}

	@Override
	public String getKey() {
		return "music_playlist_id";
	}

	@Override
	public String[] getTags() {
		return new String[]{"music", "playlist"};
	}

	@Override
	public String getDefault() {
		return "0";
	}

	@Override
	public String[] getDescription() {
		return new String[]{
				"used to store the last used playlist "
		};
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}
}