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

package emily.guildsettings.music;

import emily.guildsettings.AbstractGuildSetting;
import emily.guildsettings.types.NumberBetweenSettingType;


public class SettingMusicVolume extends AbstractGuildSetting<NumberBetweenSettingType> {
    @Override
    protected NumberBetweenSettingType getSettingsType() {
        return new NumberBetweenSettingType(0, 100);
    }

    @Override
    public String getKey() {
        return "music_volume";
    }

    @Override
    public String[] getTags() {
        return new String[]{"music", "volume"};
    }

    @Override
    public String getDefault() {
        return "10";
    }

    @Override
    public String[] getDescription() {
        return new String[]{
                "sets the default volume of the music player",
                "So the next time the bot connects it starts with this volume",
                "",
                "Accepts a value between 0 and 100"
        };
    }
}