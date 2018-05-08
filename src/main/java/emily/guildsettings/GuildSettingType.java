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

package emily.guildsettings;

import emily.guildsettings.types.BooleanSettingType;
import emily.guildsettings.types.NoSettingType;
import emily.guildsettings.types.NumberBetweenSettingType;
import emily.guildsettings.types.RoleSettingType;
import emily.guildsettings.types.TextChannelSettingType;
import emily.guildsettings.types.VoiceChannelSettingType;
import emily.main.BotConfig;

public class GuildSettingType {
    public static final IGuildSettingType INTERNAL = new NoSettingType();
    public static final IGuildSettingType TOGGLE = new BooleanSettingType();
    public static final IGuildSettingType PERCENTAGE = new NumberBetweenSettingType(0, 100);
    public static final IGuildSettingType VOLUME = new NumberBetweenSettingType(0, BotConfig.MUSIC_MAX_VOLUME);
    public static final IGuildSettingType TEXT_CHANNEL_OPTIONAL = new TextChannelSettingType(true);
    public static final IGuildSettingType TEXT_CHANNEL_MANDATORY = new TextChannelSettingType(false);
    public static final IGuildSettingType ROLE_OPTIONAL = new RoleSettingType(true);
    public static final IGuildSettingType ROLE_MANDATORY = new RoleSettingType(false);
    public static final IGuildSettingType VOICE_CHANNEL_OPTIONAL = new VoiceChannelSettingType(true);
    public static final IGuildSettingType VOICE_CHANNEL_MANDATORY = new VoiceChannelSettingType(false);
}
