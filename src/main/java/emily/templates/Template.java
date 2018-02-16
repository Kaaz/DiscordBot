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
import emily.guildsettings.GSetting;
import emily.handler.GuildSettings;
import emily.main.BotConfig;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;

public class Template {
    final private TemplateArgument[] templateArguments;
    final private TemplateArgument[] optionalArgs;
    private String key;

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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
        return formatFull(0, false, vars);
    }

    public String formatGuild(MessageChannel channel, Object... vars) {
        if (channel.getType().equals(ChannelType.TEXT)) {
            return formatFull(((TextChannel) channel).getGuild().getIdLong(), false, vars);
        }
        return formatFull(0, false, vars);
    }

    public String formatGuild(long guildId, Object... vars) {
        return formatFull(guildId, false, vars);
    }

    public String formatFull(long guildId, boolean forceDebug, Object... vars) {
        if (templateArguments.length == 0 && optionalArgs.length == 0) {
            if (guildId == 0) {
                return TemplateCache.getGlobal(getKey());
            }
            return TemplateCache.getGuild(CGuild.getCachedId(guildId), getKey());
        }
        boolean showTemplates = forceDebug || BotConfig.SHOW_KEYPHRASE;
        if (!forceDebug && guildId > 0) {
            showTemplates = "true".equals(GuildSettings.get(guildId).getOrDefault(GSetting.SHOW_TEMPLATES));
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
                    sb.append(String.format("%-17s -> %s\n", " |-> value -> ", arg.parse(env)));
                }
            }
            if (optionalArgs.length > 0) {
                sb.append("\nOptional:\n\n");
                for (TemplateArgument arg : optionalArgs) {
                    sb.append(String.format("%-17s -> %s\n", arg.getPattern(), arg.getDescription()));
                    String var = arg.parse(env);
                    if (!var.isEmpty()) {
                        sb.append(String.format("%-17s -> %s\n", " |-> value -> ", arg.parse(env)));
                    }
                }
            }
            sb.append("```");
            return sb.toString();
        } else {
            String tmp = guildId > 0 ? TemplateCache.getGuild(CGuild.getCachedId(guildId), getKey()) : TemplateCache.getGlobal(getKey());
            for (TemplateArgument arg : templateArguments) {
                tmp = tmp.replace(arg.getPattern(), arg.parse(env));
            }
            for (TemplateArgument arg : optionalArgs) {
                if (tmp.contains(arg.getPattern())) {
                    tmp = tmp.replace(arg.getPattern(), arg.parse(env));
                }
            }
            return tmp;
        }
    }
}
