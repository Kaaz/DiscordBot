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
import emily.main.Config;


public class SettingBotShowTemplates extends AbstractGuildSetting<BooleanSettingType> {
    @Override
    protected BooleanSettingType getSettingsType() {
        return new BooleanSettingType();
    }

    @Override
    public String getKey() {
        return "show_templates";
    }

    @Override
    public String[] getTags() {
        return new String[]{"bot", "debug", "template"};
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getDefault() {
        return Config.SHOW_KEYPHRASE ? "true" : "false";
    }

    @Override
    public String[] getDescription() {
        return new String[]{
                "Show which templates are being used on places.",
                "",
                "valid values: ",
                "true       -> Shows the keyphrases being used ",
                "false      -> Shows normal text ",
                "",
                "for instance if you don't have permission to access a command:",
                "",
                "setting this to true would show:",
                "no_permission",
                "",
                "false would show:",
                "You don't have permission to use that!",
        };
    }
}
