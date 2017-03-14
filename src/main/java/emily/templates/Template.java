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

package emily.templates;

import emily.db.controllers.CGuild;
import emily.guildsettings.bot.SettingBotShowTemplates;
import emily.handler.GuildSettings;
import emily.main.Config;

public class Template {
    private String key;
    final private TemplateArgument[] templateArguments;
    final private TemplateArgument[] optionalArgs;

    public Template(TemplateArgument... templateArguments) {
        this(templateArguments, null);
    }

    public Template(TemplateArgument[] requiredArguments, TemplateArgument[] optionalArgs) {
        if (requiredArguments == null) {
            templateArguments = new TemplateArgument[]{};
        } else {
            templateArguments = requiredArguments;
        }
        if (optionalArgs == null) {
            this.optionalArgs = new TemplateArgument[]{};
        } else {
            this.optionalArgs = optionalArgs;
        }
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public TemplateArgument[] getRequiredArguments() {
        return templateArguments;
    }

    public boolean isValidTemplate(String template) {
        if (template == null || template.isEmpty()) {
            return false;
        }
        if (templateArguments.length == 0) {
            return true;
        }
        for (TemplateArgument argument : templateArguments) {
            if (!template.contains(argument.getPattern())) {
                return false;
            }
        }
        return true;
    }

    public String format(Object... vars) {
        return formatFull(null, false, vars);
    }

    public String formatGuild(String guildId, Object... vars) {
        return formatFull(guildId, false, vars);
    }

    public String formatFull(String guildId, boolean forceDebug, Object... vars) {
        if (templateArguments.length == 0 & optionalArgs.length == 0) {
            return TemplateCache.getGlobal(getKey());
        }
        boolean showTemplates = forceDebug || Config.SHOW_KEYPHRASE;
        if (!forceDebug && guildId != null && !guildId.isEmpty()) {
            showTemplates = "true".equals(GuildSettings.get(guildId).getOrDefault(SettingBotShowTemplates.class));
        }
        TemplateVariables env = TemplateVariables.create(vars);
        if (showTemplates) {
            StringBuilder sb = new StringBuilder();
            sb.append("Template: `").append(getKey()).append("`");
            sb.append("\nAvailable arguments:\n```\n");
            if (templateArguments.length > 0) {
                sb.append("Required:\n\n");
                for (TemplateArgument arg : templateArguments) {
                    sb.append(String.format("%-17s -> %s\n", arg.getPattern(), arg.getDescription()));
                }
            }
            if (optionalArgs.length > 0) {
                sb.append("\nOptional:\n\n");
                for (TemplateArgument arg : optionalArgs) {
                    sb.append(String.format("%-17s -> %s\n", arg.getPattern(), arg.getDescription()));
                }
            }
            sb.append("```");
            return sb.toString();
        } else {
            String tmp = guildId != null && !guildId.isEmpty() ? TemplateCache.getGuild(CGuild.getCachedId(guildId), getKey()) : TemplateCache.getGlobal(getKey());
            for (TemplateArgument arg : templateArguments) {
                tmp = tmp.replace(arg.getPattern(), arg.parse(env));
            }
            for (TemplateArgument arg : optionalArgs) {
                tmp = tmp.replace(arg.getPattern(), arg.parse(env));
            }
            return tmp;
        }
    }
}
