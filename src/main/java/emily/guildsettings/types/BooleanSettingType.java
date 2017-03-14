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

package emily.guildsettings.types;

import emily.guildsettings.IGuildSettingType;
import emily.util.Emojibet;
import emily.util.Misc;
import net.dv8tion.jda.core.entities.Guild;

/**
 * boolean settings type
 * {@link Misc#isFuzzyTrue(String)}   yes
 * {@link Misc#isFuzzyFalse(String)}  no
 */
public class BooleanSettingType implements IGuildSettingType {
    @Override
    public String typeName() {
        return "toggle";
    }

    @Override
    public boolean validate(Guild guild, String value) {
        return value != null && (Misc.isFuzzyTrue(value) || Misc.isFuzzyFalse(value));
    }

    @Override
    public String fromInput(Guild guild, String value) {
        return Misc.isFuzzyTrue(value) ? "true" : "false";
    }

    @Override
    public String toDisplay(Guild guild, String value) {
        return "true".equals(value) ? Emojibet.OKE_SIGN : Emojibet.X;
    }
}
