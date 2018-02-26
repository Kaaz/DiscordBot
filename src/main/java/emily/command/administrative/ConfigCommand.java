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

package emily.command.administrative;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import emily.command.CommandReactionListener;
import emily.command.CommandVisibility;
import emily.command.meta.ICommandReactionListener;
import emily.command.meta.PaginationInfo;
import emily.command.meta.AbstractCommand;
import emily.guildsettings.DefaultGuildSettings;
import emily.guildsettings.GSetting;
import emily.handler.GuildSettings;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.templates.Templates;
import emily.util.DisUtil;
import emily.util.Emojibet;
import emily.util.Misc;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * !config
 * gets/sets the configuration of the bot
 */
public class ConfigCommand extends AbstractCommand implements ICommandReactionListener<PaginationInfo> {
    public static final int CFG_PER_PAGE = 15;

    public ConfigCommand() {
        super();
    }

    private static MessageEmbed makeEmbedConfig(Guild guild, int activePage) {
        EmbedBuilder b = new EmbedBuilder();
        List<String> keys = DefaultGuildSettings.getWritableKeys();
        Collections.sort(keys);
        int maxPage = (int) Math.ceil((double) keys.size() / (double) CFG_PER_PAGE);
        activePage = Math.max(0, Math.min(maxPage - 1, activePage - 1));
        int endIndex = activePage * CFG_PER_PAGE + CFG_PER_PAGE;
        int elements = 0;
        for (int i = activePage * CFG_PER_PAGE; i < keys.size() && i < endIndex; i++) {
            String key = keys.get(i);
            b.addField(key.toLowerCase(), GuildSettings.get(guild.getId()).getDisplayValue(guild, key), true);
            elements++;
        }
        if (elements % 3 == 2) {
            b.addBlankField(true);
        }
        String commandPrefix = DisUtil.getCommandPrefix(guild);
        b.setFooter("Page " + (activePage + 1) + " / " + maxPage + " | Press the buttons for other pages", null);
        b.setDescription(String.format("To see more details about a setting:\n" +
                "`%1$scfg settingname`" + "\n" + "\n", commandPrefix));
        b.setTitle("Current Settings for " + guild.getName() + " [" + (1 + activePage) + " / " + maxPage + "]", null);
        return b.build();
    }

    @Override
    public String getDescription() {
        return "Gets/sets the configuration of the bot";
    }

    @Override
    public String getCommand() {
        return "config";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "config                    //overview",
                "config page <number>      //show page <number>",
                "config tags               //see what tags exist",
                "config tag <tagname>      //show settings with tagname",
                "config <property>         //check details of property",
                "config <property> <value> //sets property",
                "",
                "config reset yesimsure    //resets the configuration to the default settings",
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "setting", "cfg"
        };
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        Guild guild;
        SimpleRank rank = bot.security.getSimpleRank(author, channel);
        if (rank.isAtLeast(SimpleRank.BOT_ADMIN) && args.length >= 1 && DisUtil.matchesGuildSearch(args[0])) {
            guild = DisUtil.findGuildBy(args[0], bot.getContainer());
            if (guild == null) {
                return Templates.config.cant_find_guild.formatGuild(channel);
            }
            args = Arrays.copyOfRange(args, 1, args.length);
        } else {
            guild = ((TextChannel) channel).getGuild();
        }

        if (!rank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
            return Templates.no_permission.formatGuild(channel);
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("reset")) {
            if (args.length > 1 && args[1].equalsIgnoreCase("yesimsure")) {
                GuildSettings.get(guild).reset();
                return Templates.config.reset_success.formatGuild(channel);
            }
            return Templates.config.reset_warning.formatGuild(channel);
        }
        String tag = null;
        if (args.length > 0) {
            if (args[0].equals("tags")) {
                return "The following tags exist for settings: " + "\n" + "\n" +
                        Joiner.on(", ").join(DefaultGuildSettings.getAllTags()) + "\n" + "\n" +
                        "`" + DisUtil.getCommandPrefix(channel) + "cfg tag tagname` to see settings with tagname";
            }
            if (args[0].equals("tag") && args.length > 1) {
                tag = args[1].toLowerCase();
            }
        }
        if (args.length == 0 || tag != null || args.length > 0 && args[0].equals("page")) {
            String[] settings = GuildSettings.get(guild).getSettings();
            ArrayList<String> keys = new ArrayList<>(DefaultGuildSettings.getAllKeys());
            Collections.sort(keys);
            int activePage = 0;
            int maxPage = 1 + DefaultGuildSettings.countSettings(false) / CFG_PER_PAGE;
            if (PermissionUtil.checkPermission((TextChannel) channel, ((TextChannel) channel).getGuild().getSelfMember(), Permission.MESSAGE_EMBED_LINKS)) {
                if (args.length > 1 && args[0].equals("page")) {
                    activePage = Math.max(0, Math.min(maxPage - 1, Misc.parseInt(args[1], 0) - 1));
                }
                bot.queue.add(channel.sendMessage(makeEmbedConfig(guild, activePage)),
                        message ->
                                bot.commandReactionHandler.addReactionListener(((TextChannel) channel).getGuild().getIdLong(), message,
                                        getReactionListener(author.getIdLong(), new PaginationInfo(1, maxPage, guild))));

                return "";
            }

            String ret = "Current Settings for " + guild.getName() + "\n" + "\n";
            if (tag != null) {
                ret += "Only showing settings with the tag `" + tag + "`" + "\n";
            }
            ret += ":information_source: Settings indicated with a `*` are different from the default value" + "\n" + "\n";
            String cfgFormat = "`\u200B%-24s:`  %s" + "\n";
            boolean isEmpty = true;
            for (int i = activePage * CFG_PER_PAGE; i < keys.size() && i < activePage * CFG_PER_PAGE + CFG_PER_PAGE; i++) {
                String key = keys.get(i);
                GSetting gSetting = GSetting.valueOf(key);
                if (DefaultGuildSettings.get(key).isInternal()) {
                    if (!rank.isAtLeast(SimpleRank.BOT_ADMIN)) {
                        continue;
                    }
                }
                if (tag != null && !DefaultGuildSettings.get(key).hasTag(tag)) {
                    continue;
                }
                String indicator = "  ";
                if (rank.isAtLeast(SimpleRank.BOT_ADMIN) && DefaultGuildSettings.get(key).isInternal()) {
                    indicator = "r ";
                } else if (!settings[gSetting.ordinal()].equals(DefaultGuildSettings.getDefault(key))) {
                    indicator = "* ";
                }
                ret += String.format(cfgFormat, indicator + key, GuildSettings.get(guild.getId()).getDisplayValue(guild, key));
                isEmpty = false;
            }
            if (isEmpty && tag != null) {
                return "No settings found matching the tag `" + tag + "`";
            }

            return ret;
        }


        if (!DefaultGuildSettings.isValidKey(args[0])) {
            return Templates.command.config.key_not_exists.formatGuild(channel);
        }
        if (DefaultGuildSettings.get(args[0]).isInternal() && !rank.isAtLeast(SimpleRank.BOT_ADMIN)) {
            return Templates.command.config.key_read_only.formatGuild(channel);
        }

        if (args.length >= 2) {
            StringBuilder newValueBuilder = new StringBuilder(args[1]);
            for (int i = 2; i < args.length; i++) {
                newValueBuilder.append(" ").append(args[i]);
            }
            String newValue = newValueBuilder.toString();
            if (newValue.length() > 64) {
                newValue = newValue.substring(0, 64);
            }
            if (args[0].equals("bot_listen") && args[1].equals("mine")) {
                bot.queue.add(channel.sendMessage(Emojibet.WARNING + " I will only listen to the configured `bot_channel`. If you rename the channel, you might not be able to access me anymore. " +
                        "You can reset by typing `@" + channel.getJDA().getSelfUser().getName() + " reset yesimsure`"));
            }

            if (GuildSettings.get(guild).set(guild, args[0], newValue)) {
                return Templates.command.config.key_modified.formatGuild(channel);
            }
        }

        String tblContent = "";
        GuildSettings setting = GuildSettings.get(guild);
        tblContent += setting.getDescription(args[0]);
        return "Config help for **" + args[0] + "**" + "\n" + "\n" +
                "Current value: \"**" + GuildSettings.get(guild.getId()).getDisplayValue(guild, args[0]) + "**\"" + "\n" +
                "Default value: \"**" + setting.getDefaultValue(args[0]) + "**\"" + "\n" + "\n" +
                "Description: " + "\n" +
                Misc.makeTable(tblContent) +
                "To set it back to default: `" + DisUtil.getCommandPrefix(channel) + "cfg " + args[0] + " " + setting.getDefaultValue(args[0]) + "`";
    }

    @Override
    public CommandReactionListener<PaginationInfo> getReactionListener(long userId, PaginationInfo data) {

        CommandReactionListener<PaginationInfo> listener = new CommandReactionListener<>(userId, data);
        listener.setExpiresIn(TimeUnit.MINUTES, 2);
        listener.registerReaction(Emojibet.PREV_TRACK, o -> {
            if (listener.getData().previousPage()) {
                o.editMessage(new MessageBuilder().setEmbed(makeEmbedConfig(data.getGuild(), listener.getData().getCurrentPage())).build()).complete();
            }
        });
        listener.registerReaction(Emojibet.NEXT_TRACK, o -> {
            if (listener.getData().nextPage()) {
                o.editMessage(new MessageBuilder().setEmbed(makeEmbedConfig(data.getGuild(), listener.getData().getCurrentPage())).build()).complete();
            }
        });
        return listener;
    }
}