package discordbot.command.administrative;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import discordbot.command.CommandReactionListener;
import discordbot.command.CommandVisibility;
import discordbot.command.ICommandReactionListener;
import discordbot.command.PaginationInfo;
import discordbot.core.AbstractCommand;
import discordbot.guildsettings.DefaultGuildSettings;
import discordbot.handler.GuildSettings;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.DisUtil;
import discordbot.util.Emojibet;
import discordbot.util.Misc;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * !config
 * gets/sets the configuration of the bot
 */
public class SetConfig extends AbstractCommand implements ICommandReactionListener {
	public static final int CFG_PER_PAGE = 24;

	public SetConfig() {
		super();
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
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		Guild guild;
		SimpleRank rank = bot.security.getSimpleRank(author, channel);
		if (rank.isAtLeast(SimpleRank.BOT_ADMIN) && args.length >= 1 && DisUtil.matchesGuildSearch(args[0])) {
			guild = DisUtil.findGuildBy(args[0], bot.getContainer());
			if (guild == null) {
				return Template.get("command_config_cant_find_guild");
			}
			args = Arrays.copyOfRange(args, 1, args.length);
		} else {
			guild = ((TextChannel) channel).getGuild();
		}

		if (!rank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
			return Template.get("command_config_no_permission");
		}
		if (args.length > 0 && args[0].equalsIgnoreCase("reset")) {
			if (args.length > 1 && args[1].equalsIgnoreCase("yesimsure")) {
				GuildSettings.get(guild).reset();
				return Template.get(channel, "command_config_reset_success");
			}
			return Template.get(channel, "command_config_reset_warning");
		}
		String tag = null;
		if (args.length > 0) {
			if (args[0].equals("tags")) {
				return "The following tags exist for settings: " + Config.EOL + Config.EOL +
						Joiner.on(", ").join(DefaultGuildSettings.getAllTags()) + Config.EOL + Config.EOL +
						"`" + DisUtil.getCommandPrefix(channel) + "cfg tag tagname` to see settings with tagname";
			}
			if (args[0].equals("tag") && args.length > 1) {
				tag = args[1].toLowerCase();
			}
		}
		if (args.length == 0 || tag != null || args.length > 0 && args[0].equals("page")) {
			EmbedBuilder b = new EmbedBuilder();
			Map<String, String> settings = GuildSettings.get(guild).getSettings();
			ArrayList<String> keys = new ArrayList<>(settings.keySet());
			Collections.sort(keys);
			int maxPage = 1 + keys.size() / CFG_PER_PAGE;
			int activePage = 0;
			if (args.length > 1 && args[0].equals("page")) {
				activePage = Math.max(0, Math.min(maxPage - 1, Misc.parseInt(args[1], 0) - 1));
			}
			String ret = "Current Settings for " + guild.getName() + Config.EOL + Config.EOL;
			if (tag != null) {
				ret += "Only showing settings with the tag `" + tag + "`" + Config.EOL;
			}
			ret += ":information_source: Settings indicated with a `*` are different from the default value" + Config.EOL + Config.EOL;
			String cfgFormat = "`\u200B%-24s:`  %s" + Config.EOL;
			boolean isEmpty = true;
			for (int i = activePage * CFG_PER_PAGE; i < keys.size() && i < activePage * CFG_PER_PAGE + CFG_PER_PAGE; i++) {
				String key = keys.get(i);
				if (DefaultGuildSettings.get(key).isReadOnly()) {
					if (!rank.isAtLeast(SimpleRank.BOT_ADMIN)) {
						continue;
					}
				}
				if (tag != null && !DefaultGuildSettings.get(key).hasTag(tag)) {
					continue;
				}
				String indicator = "  ";
				if (rank.isAtLeast(SimpleRank.BOT_ADMIN) && DefaultGuildSettings.get(key).isReadOnly()) {
					indicator = "r ";
				} else if (!settings.get(key).equals(DefaultGuildSettings.getDefault(key))) {
					indicator = "* ";
				}
				ret += String.format(cfgFormat, indicator + key, GuildSettings.get(guild.getId()).getDisplayValue(guild, key));
				b.addField(key, GuildSettings.get(guild.getId()).getDisplayValue(guild, key), true);
				isEmpty = false;
			}
			if (isEmpty && tag != null) {
				return "No settings found matching the tag `" + tag + "`";
			}
			b.setFooter("Page " + (activePage + 1) + " / " + maxPage + " | Press the buttons for other pages | " + DisUtil.getCommandPrefix(channel) + "cfg page <number>", null);
			String commandPrefix = DisUtil.getCommandPrefix(guild);
			b.setDescription(String.format(((tag != null) ? "only showing settings with the tag " + tag + Config.EOL : "") +
					"To see more details about a setting:" + Config.EOL +
					"`%1$scfg settingname`" + Config.EOL + Config.EOL, commandPrefix));
			b.setTitle("Current Settings for " + guild.getName());
			if (PermissionUtil.checkPermission((TextChannel) channel, guild.getSelfMember(), Permission.MESSAGE_EMBED_LINKS)) {
				channel.sendMessage(b.build()).queue();
				return "";
			}
			return ret;
		}


		if (!DefaultGuildSettings.isValidKey(args[0])) {
			return Template.get("command_config_key_not_exists");
		}
		if (DefaultGuildSettings.get(args[0]).isReadOnly() && !rank.isAtLeast(SimpleRank.BOT_ADMIN)) {
			return Template.get("command_config_key_read_only");
		}

		if (args.length >= 2) {
			String newValue = args[1];
			for (int i = 2; i < args.length; i++) {
				newValue += " " + args[i];
			}
			if (newValue.length() > 64) {
				newValue = newValue.substring(0, 64);
			}
			if (args[0].equals("bot_listen") && args[1].equals("mine")) {
				channel.sendMessage(Emojibet.WARNING + " I will only listen to the configured `bot_channel`. If you rename the channel, you might not be able to access me anymore. " +
						"You can reset by typing `@" + channel.getJDA().getSelfUser().getName() + " reset yesimsure`").queue();
			}
			if (GuildSettings.get(guild).set(guild, args[0], newValue)) {
				bot.getContainer().getShardFor(guild.getId()).clearChannels(guild);
				return Template.get("command_config_key_modified");
			}
		}

		String tblContent = "";
		GuildSettings setting = GuildSettings.get(guild);
		for (String s : setting.getDescription(args[0])) {
			tblContent += s + Config.EOL;
		}
		return "Config help for **" + args[0] + "**" + Config.EOL + Config.EOL +
				"Current value: \"**" + GuildSettings.get(guild.getId()).getDisplayValue(guild, args[0]) + "**\"" + Config.EOL +
				"Default value: \"**" + setting.getDefaultValue(args[0]) + "**\"" + Config.EOL + Config.EOL +
				"Description: " + Config.EOL +
				Misc.makeTable(tblContent);
	}

	@Override
	public CommandReactionListener getListenObject() {

		int maxPage = 1 + DefaultGuildSettings.countSettings() / CFG_PER_PAGE;
		CommandReactionListener<PaginationInfo> listener = new CommandReactionListener<>(new PaginationInfo(1, maxPage));
		listener.registerReaction(Emojibet.PREV_TRACK, o -> {
			if (listener.getData().previousPage()) {
				o.editMessage("PAGE " + listener.getData().getCurrentPage());
			}
		});
		listener.registerReaction(Emojibet.NEXT_TRACK, o -> {
			if (listener.getData().nextPage()) {
				o.editMessage("PAGE " + listener.getData().getCurrentPage());
			}
		});
		return null;
	}
}