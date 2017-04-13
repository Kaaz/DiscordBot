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


public class SettingMusicResultPicker extends AbstractGuildSetting<NumberBetweenSettingType> {
    @Override
    protected NumberBetweenSettingType getSettingsType() {
        return new NumberBetweenSettingType(1, 5);
    }

    @Override
    public String getKey() {
        return "music_result_picker";
    }

    @Override
    public String[] getTags() {
        return new String[]{"music", "option"};
    }

    @Override
    public String getDefault() {
        return "1";
    }

    @Override
    public String[] getDescription() {
        return new String[]{
                "the amount of results the `play` command returns",
                "",
                "If its set to 1, it will always use the first result (no manual choice)",
                "",
                "If its set higher (max 5) it will respond with reactions where each button is a choice",
                "Note: This setting does require the add reactions permission",
        };
    }
}