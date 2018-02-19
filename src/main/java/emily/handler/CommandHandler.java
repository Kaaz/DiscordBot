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

package emily.handler;

import emily.command.CommandCategory;
import emily.command.CommandVisibility;
import emily.command.ICommandCooldown;
import emily.core.AbstractCommand;
import emily.db.WebDb;
import emily.db.controllers.CBlacklistCommand;
import emily.db.controllers.CBotEvent;
import emily.db.controllers.CCommandCooldown;
import emily.db.controllers.CCommandLog;
import emily.db.controllers.CGuild;
import emily.db.controllers.CUser;
import emily.db.model.OBlacklistCommand;
import emily.db.model.OBotEvent;
import emily.db.model.OCommandCooldown;
import emily.guildsettings.GSetting;
import emily.main.BotConfig;
import emily.main.DiscordBot;
import emily.main.Launcher;
import emily.templates.Templates;
import emily.util.DisUtil;
import emily.util.Emojibet;
import emily.util.TimeUtil;
import emoji4j.EmojiUtils;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles all the commands
 */
public class CommandHandler {

    public final static String ALL_COMMANDS = "all-commands";
    private static final HashMap<String, AbstractCommand> commands = new HashMap<>();
    private static final HashMap<String, AbstractCommand> commandsAlias = new HashMap<>();
    private static final Map<String, String> customCommands = new ConcurrentHashMap<>();
    private static final Map<Integer, Map<String, String>> guildCommands = new ConcurrentHashMap<>();
    private static final Map<Integer, Map<Long, Map<String, Boolean>>> commandBlacklist = new ConcurrentHashMap<>();

    /**
     * checks if the the message in channel is a command
     *
     * @param channel        the channel the message came from
     * @param msg            the message
     * @param mentionMe      the user mention string
     * @param mentionMeAlias the nickname
     * @return whether or not the message is a command
     */
    public static boolean isCommand(TextChannel channel, String msg, String mentionMe, String mentionMeAlias) {
        return msg.startsWith(DisUtil.getCommandPrefix(channel)) || msg.startsWith(mentionMe) || msg.startsWith(mentionMeAlias);
    }

    public static void removeGuild(int guildId) {
        if (guildCommands.containsKey(guildId)) {
            guildCommands.remove(guildId);
        }
    }

    /**
     * directs the command to the right class
     *
     * @param bot             The bot instance
     * @param channel         which channel
     * @param author          author
     * @param incomingMessage message
     */
    public static void process(DiscordBot bot, MessageChannel channel, User author, Message incomingMessage) {
        String outMsg = "";
        boolean commandSuccess = true;
        boolean startedWithMention = false;
        int guildId = 0;
        String inputMessage = incomingMessage.getContentRaw();
        String commandUsed = "-";
        if (inputMessage.startsWith(bot.mentionMe)) {
            inputMessage = inputMessage.replace(bot.mentionMe, "").trim();
            startedWithMention = true;
        } else if (inputMessage.startsWith(bot.mentionMeAlias)) {
            inputMessage = inputMessage.replace(bot.mentionMeAlias, "").trim();
            startedWithMention = true;
        }

        if (channel instanceof TextChannel) {
            guildId = CGuild.getCachedId(((TextChannel) channel).getGuild().getIdLong());
            if (!((TextChannel) channel).canTalk()) {
                return;
            }
        }
        //
        String[] input = inputMessage.split("\\s+", 2);// (?:([^\s\"]+)|\"((?:\w+|\\\"|[^\"])+)")
        String[] args;
        if (input.length == 2) {
            args = input[1].split(" +");
        } else {
            args = new String[0];
        }
        input[0] = DisUtil.filterPrefix(input[0], channel).toLowerCase();
        if (commands.containsKey(input[0]) || commandsAlias.containsKey(input[0])) {
            AbstractCommand command = commands.containsKey(input[0]) ? commands.get(input[0]) : commandsAlias.get(input[0]);
            commandUsed = command.getCommand();
            long cooldown = getCommandCooldown(command, author, channel);
            if (command.canBeDisabled() && isDisabled(guildId, channel.getIdLong(), command.getCommand())) {
                commandSuccess = false;
                if (GuildSettings.getFor(channel, GSetting.SHOW_UNKNOWN_COMMANDS).equals("true")) {
                    outMsg = Templates.command.is_blacklisted.format(input[0]);
                }
            } else if (cooldown > 0) {
                outMsg = Templates.command.on_cooldown.format(TimeUtil.getRelativeTime((System.currentTimeMillis() / 1000L) + cooldown, false));

            } else if (!hasRightVisibility(channel, command.getVisibility())) {
                if (channel instanceof PrivateChannel) {
                    outMsg = Templates.command.not_for_private.formatGuild(channel);
                } else {
                    outMsg = Templates.command.not_for_public.formatGuild(channel);
                }
            } else {
                String commandOutput;
                if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
                    commandOutput = commands.get("help").execute(bot, new String[]{input[0]}, channel, author, incomingMessage);
                } else {
                    commandOutput = command.execute(bot, args, channel, author, incomingMessage);
                }
                if (!commandOutput.isEmpty()) {
                    outMsg = commandOutput;
                }
                if (BotConfig.BOT_COMMAND_LOGGING) {
                    StringBuilder usedArguments = new StringBuilder();
                    for (String arg : args) {
                        usedArguments.append(arg).append(" ");
                    }
                    if (channel instanceof TextChannel) {
                        CCommandLog.saveLog(CUser.getCachedId(author.getIdLong(), EmojiUtils.shortCodify(author.getName())),
                                CGuild.getCachedId(((TextChannel) channel).getGuild().getIdLong()),
                                command.getCommand(),
                                EmojiUtils.shortCodify(usedArguments.toString()).trim());
                    }
                }
            }
        } else if (customCommands.containsKey(input[0])) {
            commandUsed = input[0];
            outMsg = DisUtil.replaceTags(customCommands.get(input[0]), author, channel, args);
        } else if (guildCommands.containsKey(guildId) && guildCommands.get(guildId).containsKey(input[0])) {
            commandUsed = "custom:" + input[0];
            outMsg = DisUtil.replaceTags(guildCommands.get(guildId).get(input[0]), author, channel, args);
        } else if (startedWithMention && BotConfig.BOT_CHATTING_ENABLED) {
            commandSuccess = false;
            channel.sendTyping().queue();
            outMsg = author.getAsMention() + ", " + bot.chatBotHandler.chat((guildId > 0 ? CGuild.getCachedDiscordId(guildId) : "private"), inputMessage);
        } else if (BotConfig.BOT_COMMAND_SHOW_UNKNOWN ||
                GuildSettings.getFor(channel, GSetting.SHOW_UNKNOWN_COMMANDS).equals("true")) {
            commandSuccess = false;
            outMsg = Templates.unknown_command.format(GuildSettings.getFor(channel, GSetting.COMMAND_PREFIX) + "help");
        }
        if (!outMsg.isEmpty()) {
            bot.out.sendAsyncMessage(channel, outMsg);
        }
        if (commandSuccess) {
            if (channel instanceof TextChannel) {
                TextChannel tc = (TextChannel) channel;
                TextChannel commandLogChannel = bot.getCommandLogChannel(tc.getGuild().getIdLong());
                if (commandLogChannel != null && commandLogChannel.canTalk()) {
                    bot.queue.add(commandLogChannel.sendMessage(
                            String.format("%s **%s#%s** used %s `%s` in %s",
                                    Emojibet.USER, author.getName(), author.getDiscriminator(), Emojibet.KEYBOARD, commandUsed, tc.getAsMention()
                            )
                    ));
                }
                Launcher.log("command executed", "bot", "command",
                        "input", incomingMessage,
                        "user-id", author.getId(),
                        "command", commandUsed,
                        "user-name", author.getName(),
                        "guild-id", tc.getGuild().getId(),
                        "guild-name", tc.getGuild().getName(),
                        "response", outMsg);
            } else {
                Launcher.log("command executed", "bot", "command-private",
                        "input", incomingMessage,
                        "user-id", author.getId(),
                        "command", commandUsed,
                        "user-name", author.getName(),
                        "response", outMsg);
            }
            CUser.registerCommandUse(CUser.getCachedId(author.getIdLong()));
        }
    }

    private static boolean hasRightVisibility(MessageChannel channel, CommandVisibility visibility) {
        if (channel instanceof PrivateChannel) {
            return visibility.isForPrivate();
        }
        return visibility.isForPublic();
    }

    /**
     * checks if a command is on cooldown and returns the amount of seconds left before next usage
     *
     * @param command the command
     * @param author  the user who sent the command
     * @param channel the channel
     * @return seconds till next use
     */
    private static long getCommandCooldown(AbstractCommand command, User author, MessageChannel channel) {
        if (command instanceof ICommandCooldown) {
            long now = System.currentTimeMillis() / 1000L;
            ICommandCooldown cd = (ICommandCooldown) command;
            String targetId;
            switch (cd.getScope()) {
                case USER:
                    targetId = author.getId();
                    break;
                case CHANNEL:
                    targetId = channel.getId();
                    break;
                case GUILD:
                    if (channel instanceof PrivateChannel) {
                        CBotEvent.insert(OBotEvent.Level.WARN, ":warning:", ":keyboard:", String.format("`%s` issued the `%s` Command with guild-scale cooldown in private channel!", author.getName(), command.getCommand()));
                    }
                    targetId = ((TextChannel) channel).getGuild().getId();
                    break;
                case GLOBAL:
                    targetId = "GLOBAL";
                    break;
                default:
                    targetId = "";
                    break;
            }
            OCommandCooldown cooldown = CCommandCooldown.findBy(command.getCommand(), targetId, cd.getScope().getId());
            if (cooldown.lastTime + cd.getCooldownDuration() <= now) {

                cooldown.command = command.getCommand();
                cooldown.targetId = targetId;
                cooldown.targetType = cd.getScope().getId();
                cooldown.lastTime = now;
                CCommandCooldown.insertOrUpdate(cooldown);
                return 0;
            }
            return cooldown.lastTime + cd.getCooldownDuration() - now;
        }
        return 0;
    }

    private static boolean isDisabled(int guildId, long channelId, String commandName) {
        if (guildId == 0) {
            return false;
        }
        if (!commandBlacklist.containsKey(guildId)) {
            return false;
        }
        if (commandBlacklist.get(guildId).containsKey(channelId)) {
            if (commandBlacklist.get(guildId).get(channelId).containsKey(commandName)) {
                return commandBlacklist.get(guildId).get(channelId).get(commandName);
            }
            if (commandBlacklist.get(guildId).get(channelId).containsKey(ALL_COMMANDS)) {
                return commandBlacklist.get(guildId).get(channelId).get(ALL_COMMANDS);
            }
            return false;
        }
        if (commandBlacklist.get(guildId).containsKey(0L)) {
            if (commandBlacklist.get(guildId).get(0L).containsKey(commandName)) {
                return commandBlacklist.get(guildId).get(0L).get(commandName);
            }
            if (commandBlacklist.get(guildId).get(0L).containsKey(ALL_COMMANDS)) {
                return commandBlacklist.get(guildId).get(0L).get(ALL_COMMANDS);
            }
            return false;
        }
        return false;
    }

    /**
     * @param key command with or without the Config.BOT_COMMAND_PREFIX
     * @return instance of Command for Key or null
     */
    public static AbstractCommand getCommand(String key) {
        if (key.startsWith(BotConfig.BOT_COMMAND_PREFIX)) {
            key = key.substring(BotConfig.BOT_COMMAND_PREFIX.length());
        }
        if (commands.containsKey(key)) {
            return commands.get(key);
        }
        if (commandsAlias.containsKey(key)) {
            return commandsAlias.get(key);
        }
        return null;
    }

    /**
     * Lists the active custom commands
     *
     * @param guildId the internal guild id
     * @return list of code-commands
     */
    public static List<String> getCustomCommands(int guildId) {
        List<String> cmds = new ArrayList<>();
        cmds.addAll(customCommands.keySet());
        if (guildCommands.containsKey(guildId)) {
            cmds.addAll(guildCommands.get(guildId).keySet());
        }
        return cmds;
    }

    public static AbstractCommand[] getCommandObjects() {
        return commands.values().toArray(new AbstractCommand[commands.values().size()]);
    }

    /**
     * Add a custom static command
     *
     * @param input  command
     * @param output return
     */
    public static void addCustomCommand(int guildId, String input, String output) {
        try {
            WebDb.get().query("DELETE FROM commands WHERE input = ? AND server = ?", input, guildId);
            WebDb.get().query("INSERT INTO commands (server,input,output) VALUES(?, ?, ?)", guildId, input, output);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        loadCustomCommands(guildId);
    }

    /**
     * Loads all the custom commands
     */
    private static void loadCustomCommands() {
        try (ResultSet r = WebDb.get().select("SELECT server,input, output FROM commands ")) {
            while (r != null && r.next()) {
                int guildId = r.getInt("server");
                if (guildId == 0) {
                    if (!commands.containsKey(r.getString("input"))) {
                        customCommands.put(r.getString("input"), r.getString("output"));
                    }
                } else {
                    if (!guildCommands.containsKey(guildId)) {
                        guildCommands.put(guildId, new ConcurrentHashMap<>());
                    }
                    guildCommands.get(guildId).put(r.getString("input"), r.getString("output"));
                }
            }
            if (r != null) {
                r.getStatement().close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadCustomCommands(int guildId) {
        removeGuild(guildId);
        try (ResultSet r = WebDb.get().select("SELECT input, output FROM commands WHERE server = ?", guildId)) {
            while (r != null && r.next()) {
                if (!guildCommands.containsKey(guildId)) {
                    guildCommands.put(guildId, new ConcurrentHashMap<>());
                }
                guildCommands.get(guildId).put(r.getString("input"), r.getString("output"));
            }

            if (r != null) {
                r.getStatement().close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Loads aliases for the commands
     */
    private static void loadAliases() {
        for (AbstractCommand command : commands.values()) {
            for (String alias : command.getAliases()) {
                if (!commandsAlias.containsKey(alias)) {
                    commandsAlias.put(alias, command);
                } else {
                    DiscordBot.LOGGER.warn(String.format("Duplicate alias found! The commands `%s` and `%s` use the alias `%s`",
                            command.getCommand(), commandsAlias.get(alias).getCommand(), alias));
                }
            }
        }
    }

    /**
     * Checks if the command category is enabled or not
     *
     * @param category the category to check
     * @return enabled?
     */
    private static boolean isCommandCategoryEnabled(CommandCategory category) {
        switch (category) {
            case MUSIC:
                return BotConfig.MODULE_ECONOMY_ENABLED;
            case ECONOMY:
                return BotConfig.MODULE_ECONOMY_ENABLED;
            case POE:
                return BotConfig.MODULE_POE_ENABLED;
            case HEARTHSTONE:
                return BotConfig.MODULE_HEARTHSTONE_ENABLED;
            default:
                return true;
        }
    }

    /**
     * Lists the active commands
     *
     * @return list of code-commands
     */
    public static String[] getCommands() {
        return commands.keySet().toArray(new String[commands.keySet().size()]);
    }

    /**
     * Initializes the commands
     */
    public static void initialize() {
        loadCommands();
        loadAliases();
        loadCustomCommands();
        reloadBlackList();
    }

    /**
     * removes a custom command
     *
     * @param guildId internal id of the guild
     * @param input   command
     */
    public static void removeCustomCommand(int guildId, String input) {
        try {
            WebDb.get().query("DELETE FROM commands WHERE input = ? AND server = ?", input, guildId);
            loadCustomCommands();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * initializes the commands
     */
    private static void loadCommands() {
        Reflections reflections = new Reflections("emily.command");
        Set<Class<? extends AbstractCommand>> classes = reflections.getSubTypesOf(AbstractCommand.class);
        for (Class<? extends AbstractCommand> s : classes) {
            try {
                if (Modifier.isAbstract(s.getModifiers())) {
                    continue;
                }
                String packageName = s.getPackage().getName();
                AbstractCommand c = s.getConstructor().newInstance();
                c.setCommandCategory(CommandCategory.fromPackage(packageName.substring(packageName.lastIndexOf(".") + 1)));
                if (!c.isEnabled()) {
                    continue;
                }
                if (!isCommandCategoryEnabled(c.getCommandCategory())) {
                    continue;
                }
                if (!commands.containsKey(c.getCommand())) {
                    commands.put(c.getCommand(), c);
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * (re-)loads the guild-specific blacklisted commands
     * Map<Integer, Map<String, HashSet<String>>>
     */
    private static void reloadBlackList() {
        commandBlacklist.clear();
        List<OBlacklistCommand> blacklisted = CBlacklistCommand.getAllBlacklisted();
        for (OBlacklistCommand item : blacklisted) {
            long channelId = Long.parseLong(item.channelId);
            if (!commandBlacklist.containsKey(item.guildId)) {
                commandBlacklist.put(item.guildId, new HashMap<>());
            }
            if (!commandBlacklist.get(item.guildId).containsKey(channelId)) {
                commandBlacklist.get(item.guildId).put(channelId, new HashMap<>());
            }
            commandBlacklist.get(item.guildId).get(channelId).put(item.command, item.blacklisted);
        }
    }

    /**
     * reloads the blacklist for a guild
     *
     * @param guildId internal guildid to reload it for
     */
    public synchronized static void reloadBlackListFor(int guildId) {
        if (commandBlacklist.containsKey(guildId)) {
            commandBlacklist.get(guildId).clear();
        } else {
            commandBlacklist.put(guildId, new HashMap<>());
        }
        List<OBlacklistCommand> blacklisted = CBlacklistCommand.getBlacklistedFor(guildId);
        for (OBlacklistCommand item : blacklisted) {
            long channelId = Long.parseLong(item.channelId);
            if (!commandBlacklist.get(item.guildId).containsKey(channelId)) {
                commandBlacklist.get(item.guildId).put(channelId, new HashMap<>());
            }
            commandBlacklist.get(item.guildId).get(channelId).put(item.command, item.blacklisted);
        }
    }
}