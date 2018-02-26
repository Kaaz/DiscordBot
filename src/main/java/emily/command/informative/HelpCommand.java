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

package emily.command.informative;

import emily.command.CommandCategory;
import emily.command.CommandReactionListener;
import emily.command.meta.ICommandReactionListener;
import emily.command.meta.AbstractCommand;
import emily.guildsettings.GSetting;
import emily.handler.CommandHandler;
import emily.handler.GuildSettings;
import emily.main.BotConfig;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.templates.Templates;
import emily.util.DisUtil;
import emily.util.Emojibet;
import emily.util.Misc;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * !help
 * help function
 */
public class HelpCommand extends AbstractCommand implements ICommandReactionListener<HelpCommand.ReactionData> {
    public HelpCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "An attempt to help out";
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "help            //shows commands grouped by categories, navigable by reactions ",
                "help full       //index of all commands, in case you don't have reactions",
                "help <command>  //usage for that command"};
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "?", "halp", "helpme", "h", "commands"
        };
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        String commandPrefix = GuildSettings.getFor(channel, GSetting.COMMAND_PREFIX);
        boolean showHelpInPM = GuildSettings.getBoolFor(channel, GSetting.HELP_IN_PM);
        if (args.length > 0 && !args[0].equals("full")) {
            AbstractCommand c = CommandHandler.getCommand(DisUtil.filterPrefix(args[0], channel));
            if (c != null) {
                String ret = " :information_source: Help > " + c.getCommand() + " :information_source:" + "\n";
                ArrayList<String> aliases = new ArrayList<>();
                aliases.add(commandPrefix + c.getCommand());
                for (String alias : c.getAliases()) {
                    aliases.add(commandPrefix + alias);
                }
                ret += Emojibet.KEYBOARD + " **Accessible through:** " + "\n" +
                        Misc.makeTable(aliases, 16, 3);
                ret += Emojibet.NOTEPAD + " **Description:** " + "\n" +
                        Misc.makeTable(c.getDescription());
                if (c.getUsage().length > 0) {
                    ret += Emojibet.GEAR + " **Usages**:```php" + "\n";
                    for (String line : c.getUsage()) {
                        ret += line + "\n";
                    }
                    ret += "```";
                }
                return ret;
            }
            return Templates.command.help.donno.formatGuild(channel);
        }
        SimpleRank userRank = bot.security.getSimpleRank(author, channel);
        String ret = "I know the following commands: " + "\n" + "\n";
        if ((args.length == 0 || !args[0].equals("full")) && channel instanceof TextChannel) {
            TextChannel textChannel = (TextChannel) channel;
            if (PermissionUtil.checkPermission(textChannel, textChannel.getGuild().getSelfMember(), Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION)) {
                HashMap<CommandCategory, ArrayList<String>> map = getCommandMap(userRank);
                CommandCategory cat = CommandCategory.getFirstWithPermission(userRank);
                bot.queue.add(channel.sendMessage(writeFancyHeader(channel, cat, map.keySet()) + styleTableCategory(cat, map.get(cat)) + writeFancyFooter(channel)),
                        msg ->
                                bot.commandReactionHandler.addReactionListener(((TextChannel) channel).getGuild().getIdLong(), msg, getReactionListener(author.getIdLong(), new ReactionData(userRank, cat))));

                return "";
            }
        }
        ret += styleTablePerCategory(getCommandMap(userRank));
        if (showHelpInPM) {
            bot.out.sendPrivateMessage(author, ret + "for more details about a command use **" + commandPrefix + "help <command>**" + "\n" +
                    ":exclamation: In private messages the prefix for commands is **" + BotConfig.BOT_COMMAND_PREFIX + "**");
            return Templates.command.help.send_private.formatGuild(channel);
        } else {
            return ret + "for more details about a command use **" + commandPrefix + "help <command>**";
        }

    }

    private HashMap<CommandCategory, ArrayList<String>> getCommandMap(SimpleRank userRank) {
        HashMap<CommandCategory, ArrayList<String>> commandList = new HashMap<>();
        if (userRank == null) {
            userRank = SimpleRank.USER;
        }
        AbstractCommand[] commandObjects = CommandHandler.getCommandObjects();
        for (AbstractCommand command : commandObjects) {
            if (!command.isListed() || !command.isEnabled() || !userRank.isAtLeast(command.getCommandCategory().getRankRequired())) {
                continue;
            }
            if (!commandList.containsKey(command.getCommandCategory())) {
                commandList.put(command.getCommandCategory(), new ArrayList<>());
            }
            commandList.get(command.getCommandCategory()).add(command.getCommand());
        }
        commandList.forEach((k, v) -> Collections.sort(v));
        return commandList;
    }

    private String styleTablePerCategory(HashMap<CommandCategory, ArrayList<String>> map) {
        StringBuilder table = new StringBuilder();
        for (CommandCategory category : CommandCategory.values()) {
            if (map.containsKey(category)) {
                table.append(styleTableCategory(category, map.get(category)));
            }
        }
        return table.toString();
    }

    private String styleTableCategory(CommandCategory category, ArrayList<String> commands) {
        return category.getEmoticon() + " " + category.getDisplayName() + "\n" + Misc.makeTable(commands);
    }

    private String writeFancyHeader(MessageChannel channel, CommandCategory active, Set<CommandCategory> categories) {
        StringBuilder header = new StringBuilder("Help Overview  | without reactions use `" + DisUtil.getCommandPrefix(channel) + "help full`\n\n|");

        for (CommandCategory cat : CommandCategory.values()) {
            if (!categories.contains(cat)) {
                continue;
            }

            if (cat.equals(active)) {
                header.append("__**" + Emojibet.DIAMOND_BLUE_SMALL).append(cat.getDisplayName()).append("**__");
            } else {
                header.append(cat.getDisplayName());
            }
            header.append(" | ");
        }
        return header + "\n\n";
    }

    private String writeFancyFooter(MessageChannel channel) {
        return "for more details about a command use `" + DisUtil.getCommandPrefix(channel) + "help <command>`\nuse the reactions below to switch between the pages";
    }

    @Override
    public CommandReactionListener<ReactionData> getReactionListener(long userId, ReactionData data) {
        CommandReactionListener<ReactionData> listener = new CommandReactionListener<>(userId, data);
        HashMap<CommandCategory, ArrayList<String>> map = getCommandMap(data.getRank());
        for (CommandCategory category : CommandCategory.values()) {
            if (map.containsKey(category)) {
                listener.registerReaction(category.getEmoticon(),
                        message -> {
                            if (listener.getData().getActiveCategory().equals(category)) {
                                return;
                            }
                            listener.getData().setActiveCategory(category);
                            message.editMessage(
                                    writeFancyHeader(message.getChannel(), category, map.keySet()) +
                                            styleTableCategory(category, map.get(category)) +
                                            writeFancyFooter(message.getChannel())).complete();
                        });
            }
        }
        return listener;
    }

    public class ReactionData {
        final SimpleRank rank;
        private CommandCategory activeCategory;

        private ReactionData(SimpleRank rank, CommandCategory activeCategory) {
            this.rank = rank;
            this.activeCategory = activeCategory;
        }

        public CommandCategory getActiveCategory() {
            return activeCategory;
        }

        public void setActiveCategory(CommandCategory activeCategory) {
            this.activeCategory = activeCategory;
        }

        public SimpleRank getRank() {
            return rank;
        }
    }
}