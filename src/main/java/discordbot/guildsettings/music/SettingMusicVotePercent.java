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
import discordbot.guildsettings.types.NumberBetweenSettingType;


public class SettingMusicVotePercent extends AbstractGuildSetting<NumberBetweenSettingType> {
    @Override
    protected NumberBetweenSettingType getSettingsType() {
        return new NumberBetweenSettingType(0, 100);
    }

    @Override
    public String getKey() {
        return "music_vote_percent";
    }

    @Override
    public String[] getTags() {
        return new String[]{"music", "vote", "skip"};
    }

    @Override
    public String getDefault() {
        return "40";
    }

    @Override
    public String[] getDescription() {
        return new String[]{
                "Percentage of users (rounded down) required to skip the currently playing track",
                "",
                "eg; when set to 25, and 5 listeners it would require 2 users to vote skip ",
                "",
                "Accepts a value between 1 and 100",
        };
    }
}