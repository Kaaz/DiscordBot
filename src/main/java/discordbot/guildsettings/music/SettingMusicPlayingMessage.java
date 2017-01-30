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
import discordbot.guildsettings.types.EnumSettingType;


public class SettingMusicPlayingMessage extends AbstractGuildSetting<EnumSettingType> {
    @Override
    protected EnumSettingType getSettingsType() {
        return new EnumSettingType("clear", "normal", "off");
    }

    @Override
    public String getKey() {
        return "music_playing_message";
    }

    @Override
    public String[] getTags() {
        return new String[]{"music", "message"};
    }

    @Override
    public String getDefault() {
        return "clear";
    }

    @Override
    public String[] getDescription() {
        return new String[]{"Clear the now playing message?",
                "clear  -> sends a message and deletes it when the song is over or skipped",
                "normal -> send the message and just leave it be",
                "off    -> don't send now playing messages",
        };
    }
}
