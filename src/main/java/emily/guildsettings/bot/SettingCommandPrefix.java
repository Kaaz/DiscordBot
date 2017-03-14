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

package emily.guildsettings.bot;

import emily.guildsettings.AbstractGuildSetting;
import emily.guildsettings.types.StringLengthSettingType;
import emily.main.Config;


public class SettingCommandPrefix extends AbstractGuildSetting<StringLengthSettingType> {
    @Override
    protected StringLengthSettingType getSettingsType() {
        return new StringLengthSettingType(1, 4);
    }

    @Override
    public String getKey() {
        return "command_prefix";
    }

    @Override
    public String[] getTags() {
        return new String[]{"bot", "prefix", "command"};
    }

    @Override
    public String getDefault() {
        return Config.BOT_COMMAND_PREFIX;
    }

    @Override
    public String[] getDescription() {
        return new String[]{"Prefix for commands (between 1 and 4 characters)"};
    }
}
