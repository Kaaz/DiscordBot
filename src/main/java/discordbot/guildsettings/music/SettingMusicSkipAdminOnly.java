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
import discordbot.guildsettings.types.BooleanSettingType;


public class SettingMusicSkipAdminOnly extends AbstractGuildSetting<BooleanSettingType> {
    @Override
    protected BooleanSettingType getSettingsType() {
        return new BooleanSettingType();
    }

    @Override
    public String getKey() {
        return "music_skip_admin_only";
    }

    @Override
    public String[] getTags() {
        return new String[]{"music", "skip", "admin"};
    }

    @Override
    public String getDefault() {
        return "false";
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public String[] getDescription() {
        return new String[]{"Only allow admins to use the skip command?",
                "",
                "true",
                "Only admins have permission to use the skip command",
                "",
                "false",
                "Everyone can use the skip command",
        };
    }
}
