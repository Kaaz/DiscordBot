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


public class SettingRoleTimeRanksPrefix extends AbstractGuildSetting<StringLengthSettingType> {
    @Override
    protected StringLengthSettingType getSettingsType() {
        return new StringLengthSettingType(3, 8);
    }

    @Override
    public String getKey() {
        return "user_time_ranks_prefix";
    }

    @Override
    public String[] getTags() {
        return new String[]{"user", "rank", "prefix"};
    }

    @Override
    public String getDefault() {
        return "[rank]";
    }

    @Override
    public String[] getDescription() {
        return new String[]{
                "The prefix of the role name for the time based role ranking",
                "Using this prefix to manage roles so make sure its somewhat unique! Or you'll have to cleanup yourself :)",
                "If you'd like to use the time based ranks make sure to set this first!",
                "",
                "The prefix can be between 3 and 8 in length"};
    }
}
