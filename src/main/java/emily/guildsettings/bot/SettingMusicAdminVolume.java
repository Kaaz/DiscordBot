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

/**
 * Made by nija123098 on 12/4/2016
 */
public class SettingMusicAdminVolume extends AbstractGuildSetting<BooleanSettingType> {
    @Override
    protected BooleanSettingType getSettingsType() {
        return new BooleanSettingType();
    }

    @Override
    public String getKey() {
        return "music_volume_admin";
    }

    @Override
    public String[] getTags() {
        return new String[]{"music", "volume", "admin"};
    }

    @Override
    public String getDefault() {
        return "false";
    }

    @Override
    public String[] getDescription() {
        return new String[]{"Require a guild admin to change the volume",
                "",
                "true -> only allow guild admins to change the bot's volume",
                "false -> allow all users to change the bot's volume"};
    }
}
