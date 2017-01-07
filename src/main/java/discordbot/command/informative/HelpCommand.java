package discordbot.command.informative;

import discordbot.command.CommandCategory;
import discordbot.command.CommandReactionListener;
import discordbot.command.ICommandReactionListener;
import discordbot.core.AbstractCommand;
import discordbot.guildsettings.bot.SettingCommandPrefix;
import discordbot.guildsettings.bot.SettingHelpInPM;
import discordbot.handler.CommandHandler;
import discordbot.handler.GuildSettings;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.DisUtil;
import discordbot.util.Emojibet;
import discordbot.util.Misc;
import net.dv8tion.jda.core.Permission;
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
public class HelpCommand extends AbstractCommand implements ICommandReactionListener<SimpleRank> {
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
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		String commandPrefix = GuildSettings.getFor(channel, SettingCommandPrefix.class);
		boolean showHelpInPM = GuildSettings.getFor(channel, SettingHelpInPM.class).equals("true");
		if (args.length > 0 && !args[0].equals("full")) {
			AbstractCommand c = CommandHandler.getCommand(DisUtil.filterPrefix(args[0], channel));
			if (c != null) {
				String ret = " :information_source: Help > " + c.getCommand() + " :information_source:" + Config.EOL;
				ArrayList<String> aliases = new ArrayList<>();
				aliases.add(commandPrefix + c.getCommand());
				for (String alias : c.getAliases()) {
					aliases.add(commandPrefix + alias);
				}
				ret += Emojibet.KEYBOARD + " **Accessible through:** " + Config.EOL +
						Misc.makeTable(aliases, 16, 3);
				ret += Emojibet.NOTEPAD + " **Description:** " + Config.EOL +
						Misc.makeTable(c.getDescription());
				if (c.getUsage().length > 0) {
					ret += Emojibet.GEAR + " **Usages**:```php" + Config.EOL;
					for (String line : c.getUsage()) {
						ret += line + Config.EOL;
					}
					ret += "```";
				}
				return ret;
			}
			return Template.get("command_help_donno");
		}
		SimpleRank userRank = bot.security.getSimpleRank(author, channel);
		String ret = "I know the following commands: " + Config.EOL + Config.EOL;
		if ((args.length == 0 || !args[0].equals("full")) && channel instanceof TextChannel) {
			TextChannel textChannel = (TextChannel) channel;
			if (PermissionUtil.checkPermission(textChannel, textChannel.getGuild().getSelfMember(), Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION)) {
				HashMap<CommandCategory, ArrayList<String>> map = getCommandMap(userRank);
				CommandCategory cat = CommandCategory.getFirstWithPermission(userRank);
				channel.sendMessage(writeFancyHeader(channel, cat, map.keySet()) + styleTableCategory(cat, map.get(cat)) + writeFancyFooter(channel)).queue(
						message -> bot.commandReactionHandler.addReactionListener(((TextChannel) channel).getGuild().getId(), message, getReactionListener(author.getId(), userRank))
				);
				return "";
			}
		}
		ret += styleTablePerCategory(getCommandMap(userRank));
		if (showHelpInPM) {
			bot.out.sendPrivateMessage(author, ret + "for more details about a command use **" + commandPrefix + "help <command>**" + Config.EOL +
					":exclamation: In private messages the prefix for commands is **" + Config.BOT_COMMAND_PREFIX + "**");
			return Template.get("command_help_send_private");
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
		String table = "";
		for (CommandCategory category : CommandCategory.values()) {
			if (map.containsKey(category)) {
				table += styleTableCategory(category, map.get(category));
			}
		}
		return table;
	}

	private String styleTableCategory(CommandCategory category, ArrayList<String> commands) {
		return category.getEmoticon() + " " + category.getDisplayName() + Config.EOL + Misc.makeTable(commands);
	}

	private String writeFancyHeader(MessageChannel channel, CommandCategory active, Set<CommandCategory> categories) {
		String header = "Help Overview  | without reactions use `" + DisUtil.getCommandPrefix(channel) + "help full`\n\n|";

		for (CommandCategory cat : CommandCategory.values()) {
			if (!categories.contains(cat)) {
				continue;
			}

			if (cat.equals(active)) {
				header += "__**" + Emojibet.DIAMOND_BLUE_SMALL + cat.getDisplayName() + "**__";
			} else {
				header += cat.getDisplayName();
			}
			header += " | ";
		}
		return header + "\n\n";
	}

	private String writeFancyFooter(MessageChannel channel) {
		return "for more details about a command use `" + DisUtil.getCommandPrefix(channel) + "help <command>`";
	}

	@Override
	public CommandReactionListener<SimpleRank> getReactionListener(String invokerUserId, SimpleRank rank) {
		CommandReactionListener<SimpleRank> listener = new CommandReactionListener<>(invokerUserId, rank);
		HashMap<CommandCategory, ArrayList<String>> map = getCommandMap(rank);
		for (CommandCategory category : CommandCategory.values()) {
			if (map.containsKey(category)) {
				listener.registerReaction(category.getEmoticon(),
						message -> message.editMessage(
								writeFancyHeader(message.getChannel(), category, map.keySet()) +
										styleTableCategory(category, map.get(category)) +
										writeFancyFooter(message.getChannel())).queue());
			}
		}
		return listener;
	}
}