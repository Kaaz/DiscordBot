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
import discordbot.guildsettings.types.TextChannelSettingType;


public class SettingCommandLoggingChannel extends AbstractGuildSetting<TextChannelSettingType> {
	@Override
	protected TextChannelSettingType getSettingsType() {
		return new TextChannelSettingType(true);
	}

	@Override
	public String getKey() {
		return "bot_command_logging_channel";
	}

	@Override
	public String[] getTags() {
		return new String[]{"command", "logging", "channel", "mod"};
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"The channel command usage will be logged to",
				"",
				"Example output:",
				"Kaaz#9436 has used `say` in #general",
				"aruments: this is not a test",
				"output: this is not a test",
				"",
				"Setting this to 'false' will disable it (without the quotes)",
				"",
				"To enable it, set this setting to match the channel name where you want the command logging to happen",
				"If you specify an invalid channel, this setting will disable itself"
		};
	}
}
