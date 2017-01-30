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

package discordbot.guildsettings.types;

import discordbot.guildsettings.IGuildSettingType;
import net.dv8tion.jda.core.entities.Guild;

import java.util.Collections;
import java.util.HashSet;

/**
 * enum settings type
 * the setting has to be in the list
 */
public class EnumSettingType implements IGuildSettingType {
    private final HashSet<String> options;

    public EnumSettingType(String... values) {
        options = new HashSet<>();
        Collections.addAll(options, values);
    }

    @Override
    public String typeName() {
        return "enum";
    }

    @Override
    public boolean validate(Guild guild, String value) {
        return value != null && options.contains(value.toLowerCase());
    }

    @Override
    public String fromInput(Guild guild, String value) {
        return value;
    }

    @Override
    public String toDisplay(Guild guild, String value) {
        return value;
    }
}
