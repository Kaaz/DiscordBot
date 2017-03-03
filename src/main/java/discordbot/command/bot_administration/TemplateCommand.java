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

package discordbot.command.bot_administration;

import com.vdurmont.emoji.EmojiParser;
import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CGuild;
import discordbot.guildsettings.bot.SettingBotShowTemplates;
import discordbot.handler.GuildSettings;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.templates.Template;
import discordbot.templates.TemplateArgument;
import discordbot.templates.TemplateCache;
import discordbot.templates.Templates;
import discordbot.util.Misc;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * !template
 * manages the templates
 */
public class TemplateCommand extends AbstractCommand {
    public TemplateCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "adds/removes templates";
    }

    @Override
    public String getCommand() {
        return "template";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "template <keyphrase>                  //shows all templates for a keyphrase",
                "template add <keyphrase> <text...>    //adds a template for keyphrase",
                "template search <contains>            //searches for keyphrases matching part of the <contains>",
                "template list <page>                  //lists all keyphrases",
                "template remove <keyphrase> <index>   //removes selected template for keyphrase",
                "template debug [on/off]               //enables/disabled debugging of templates",
                "",
                "There are a few keywords you can utilize in templates. These keywords will be replaced by its value ",
                "To see which variables are at your disposal:",
                "template variable",
                "",
                "for users with botadmin+, use 'template global ...' for global templates",

        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "tpl"
        };
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        SimpleRank userRank = bot.security.getSimpleRank(author, channel);
        int guildId = CGuild.getCachedId(channel);
        if (!userRank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
            return Templates.no_permission.format();
        }
        if (!userRank.isAtLeast(SimpleRank.BOT_ADMIN)) {
            if (!(channel instanceof TextChannel)) {
                return Templates.error.command_public_only.format();
            }
        } else {
            if (args.length > 1 && args[0].equals("global")) {
                args = Arrays.copyOfRange(args, 1, args.length);
                guildId = 0;
            }
        }
        if (args.length == 0) {
            String usage = ":gear: **Options**:```php" + Config.EOL;
            for (String line : getUsage()) {
                usage += line + Config.EOL;
            }
            return usage + "```";
        }
        switch (args[0]) {
            case "var":
            case "variable":
                StringBuilder sb = new StringBuilder("Template variables\n\n")
                        .append("Variables are predefined texts which are replaced based on context\n\n")
                        .append("You can use the following variables in templates:\n```\n");
                sb.append(String.format("%-18s %s\n", "Pattern", "Description"));
                sb.append(String.format("%-18s %s\n", "---", "---"));
                for (TemplateArgument argument : TemplateArgument.values()) {
                    sb.append(String.format("%-18s %s\n", argument.getPattern(), argument.getDescription()));
                }
                sb.append("```");
                return sb.toString();
            case "debug":
                if (userRank.isAtLeast(SimpleRank.GUILD_ADMIN) && channel.getType().equals(ChannelType.TEXT)) {
                    Guild guild = ((TextChannel) channel).getGuild();
                    if (args.length == 1) {
                        return "Show keyphrases: " + GuildSettings.get(guild).getDisplayValue(guild, "show_templates");
                    } else {
                        if (GuildSettings.get(guild).set(guild, SettingBotShowTemplates.class, args[1])) {
                            return "Show Keyphrases: " + GuildSettings.get(guild).getDisplayValue(guild, "show_templates");
                        }
                    }
                }
                return Templates.no_permission.format();
            case "add":
                if (args.length >= 3) {
                    String text = Misc.joinStrings(args, 2);
                    if (Templates.templateExists(args[1])) {
                        Template tmp = Templates.getByKey(args[1]);
                        if (tmp.isValidTemplate(text)) {
                            TemplateCache.add(guildId, args[1], EmojiParser.parseToAliases(text));
                            return Templates.command.template.added.format();
                        }
                        System.out.println(tmp.formatFull(CGuild.getCachedDiscordId(guildId), true));
                        return Templates.command.template.added_failed.formatGuild(CGuild.getCachedDiscordId(guildId)) + "\n\n" +
                                tmp.formatFull(CGuild.getCachedDiscordId(guildId), true);
                    }
                }
                return Templates.command.template.added_failed.format();
            case "delete":
            case "del":
            case "remove":
                if (args.length < 3 || !args[2].matches("^\\d+$")) {
                    return Templates.command.template.invalid_option.format();
                }
                int deleteIndex = Integer.parseInt(args[2]);
                List<String> templateList = TemplateCache.getAllFor(guildId, args[1]);
                if (templateList.size() > deleteIndex) {
                    TemplateCache.remove(guildId, args[1], templateList.get(deleteIndex));
                    return Templates.command.template.delete_success.format();
                }
                return Templates.command.template.delete_failed.format();
            case "list":
            case "search":
                int currentPage = 0;
                int itemsPerPage = 5;
                int uniq = Templates.uniquePhraseCount();
                int maxPage = 1 + Templates.uniquePhraseCount() / itemsPerPage;
                if (args.length >= 2 && !args[1].matches("\\d+")) {
                    List<String> allKeyphrases = Templates.getAllKeyphrases(args[1]);
                    if (allKeyphrases.isEmpty()) {
                        return "No keyphases matching `" + args[1] + "`";
                    }
                    return String.format("All keyphrases matching `%s`: ", args[1]) + Config.EOL +
                            Misc.makeTable(allKeyphrases, 50, 2);
                } else if (args.length >= 2 && args[1].matches("\\d+")) {
                    currentPage = Math.min(Math.max(0, Misc.parseInt(args[1], 0) - 1), maxPage - 1);
                }
                List<String> allKeyphrases = Templates.getAllKeyphrases(itemsPerPage, currentPage * itemsPerPage);
                if (allKeyphrases.isEmpty()) {
                    return "No keyphrases set at this moment.";
                }
                return String.format("All keyphrases: [page %s/%s]", currentPage + 1, maxPage) + Config.EOL +
                        Misc.makeTable(allKeyphrases, 50, 2);
            default:
                args[0] = args[0].toLowerCase();
                List<String> templates = TemplateCache.getAllFor(guildId, args[0]);
                if (args.length == 1) {
                    if (templates.isEmpty()) {
                        return Templates.command.template.not_found.formatGuild(CGuild.getCachedDiscordId(guildId), args[0]);
                    }
                    List<List<String>> body = new ArrayList<>();
                    int index = 0;
                    for (String template : templates) {
                        body.add(Arrays.asList(String.valueOf(index++), template));
                    }
                    return "Template overview for `" + args[0] + "`" + Config.EOL +
                            Misc.makeAsciiTable(Arrays.asList("#", "value"), body, null);
                }
                return Templates.command.template.invalid_option.format();
        }
    }
}