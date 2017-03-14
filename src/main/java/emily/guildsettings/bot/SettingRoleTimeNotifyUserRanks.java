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
import emily.guildsettings.types.EnumSettingType;


public class SettingRoleTimeNotifyUserRanks extends AbstractGuildSetting<EnumSettingType> {
    @Override
    protected EnumSettingType getSettingsType() {
        return new EnumSettingType("no", "false", "private", "public", "both");
    }

    @Override
    public String getKey() {
        return "user_time_ranks_notify";
    }

    @Override
    public String[] getTags() {
        return new String[]{"user", "rank", "warn"};
    }

    @Override
    public String getDefault() {
        return "no";
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public String[] getDescription() {
        return new String[]{
                "Send a notification whenever a user goes up a rank?",
                "no      -> Don't notify anyone, stay silent!",
                "false   -> Don't notify anyone, stay silent!",
                "private -> send a private message to the user who ranked up",
                "public  -> announce it in a channel",
                "both    -> perform both private and public actions "};
    }
}
