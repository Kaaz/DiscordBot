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
import emily.guildsettings.types.BooleanSettingType;


public class SettingRoleTimeRanks extends AbstractGuildSetting<BooleanSettingType> {
    @Override
    protected BooleanSettingType getSettingsType() {
        return new BooleanSettingType();
    }

    @Override
    public String getKey() {
        return "user_time_ranks";
    }

    @Override
    public String[] getTags() {
        return new String[]{"user", "rank", "auto"};
    }

    @Override
    public String getDefault() {
        return "false";
    }

    @Override
    public String[] getDescription() {
        return new String[]{
                "This setting will require me to have the manage role permission!",
                "Users are given a role based on their time spend in the discord server",
                "If you'd like to use the time based ranks, be sure to check out the other settings first!",
                "Setting:  Use time based ranks?",
                "true  -> yes",
                "false -> no"};
    }
}
