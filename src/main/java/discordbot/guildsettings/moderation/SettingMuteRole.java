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

package discordbot.guildsettings.moderation;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.RoleSettingType;


public class SettingMuteRole extends AbstractGuildSetting<RoleSettingType> {
	@Override
	protected RoleSettingType getSettingsType() {
		return new RoleSettingType(true);
	}

	@Override
	public String getKey() {
		return "bot_mute_role";
	}

	@Override
	public String[] getTags() {
		return new String[]{"mod", "role", "punish"};
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"This is the role which is applied to those who you use the mute command on",
				"",
				"Setting this value to false will disable the role applied with the mute command"};
	}
}