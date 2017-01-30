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

package discordbot.guildsettings;

import net.dv8tion.jda.core.entities.Guild;

import java.util.Arrays;
import java.util.HashSet;

abstract public class AbstractGuildSetting<T extends IGuildSettingType> {
    final private T type;
    private final HashSet<String> tags;

    public AbstractGuildSetting() {
        type = getSettingsType();
        tags = new HashSet<>();
        tags.addAll(Arrays.asList(getTags()));
    }

    protected abstract T getSettingsType();

    /**
     * key for the configuration
     *
     * @return keyname
     */
    public abstract String getKey();

    /**
     * The tags to initialize the setting with
     *
     * @return array of tags
     */
    public abstract String[] getTags();

    public boolean hasTag(String tagNFame) {
        return tags.contains(tagNFame);
    }

    /**
     * default value for the config
     *
     * @return default
     */
    public abstract String getDefault();

    /**
     * Description for the config
     *
     * @return short description
     */
    public abstract String[] getDescription();

    /**
     * Whether a config setting is read-only
     * Used to save guild-specific settings which are set automatically
     *
     * @return is readonly?
     */
    public boolean isReadOnly() {
        return false;
    }

    /**
     * Checks if the value is a valid setting
     *
     * @param input value to check
     * @return is it a valid value
     */
    public boolean isValidValue(Guild guild, String input) {
        return type.validate(guild, input);
    }

    public String getValue(Guild guild, String input) {
        return type.fromInput(guild, input);
    }

    public String toDisplay(Guild guild, String value) {
        return type.toDisplay(guild, value);
    }

    public String getSettingType() {
        return type.typeName();
    }
}
