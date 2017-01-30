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
import discordbot.guildsettings.types.BooleanSettingType;


public class SettingWelcomeNewUsers extends AbstractGuildSetting<BooleanSettingType> {
    @Override
    protected BooleanSettingType getSettingsType() {
        return new BooleanSettingType();
    }

    @Override
    public String getKey() {
        return "welcome_new_users";
    }

    @Override
    public String[] getTags() {
        return new String[]{"message", "welcome", "user"};
    }

    @Override
    public String getDefault() {
        return "false";
    }

    @Override
    public String[] getDescription() {
        return new String[]{"Show a welcome message to new users?",
                "Valid options:",
                "true  -> shows a welcome when a user joins or leaves the guild",
                "false -> Disabled, doesn't say anything",
                "",
                "The welcome message can be set with the template: ",
                "welcome_new_user",
                "",
                "The welcome back message can be set with the template (if the user had joined before): ",
                "welcome_back_user",
                "",
                "The leave message can be set with the template: ",
                "message_user_leaves",
                "",
                "If multiple templates are set a random one will be chosen",
                "See the template command for more details"};
    }
}
