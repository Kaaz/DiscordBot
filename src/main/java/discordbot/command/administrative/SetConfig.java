package discordbot.command.administrative;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CGuild;
import discordbot.guildsettings.DefaultGuildSettings;
import discordbot.handler.GuildSettings;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.Misc;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.*;

/**
 * !config
 * gets/sets the configuration of the bot
 */
public class SetConfig extends AbstractCommand {
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
		if (rank.isAtLeast(SimpleRank.BOT_ADMIN) && args.length >= 1 && (args[0].matches("^\\d{10,}$") || args[0].matches("i\\d+"))) {
			if (args[0].matches("i\\d+")) {
				guild = bot.client.getGuildById(CGuild.findById(Integer.parseInt(args[0].substring(1))).discord_id);
			} else {
				guild = bot.client.getGuildById(args[0]);
			}
			if (guild == null) {
				return Template.get("command_config_cant_find_guild");
			}
			args = Arrays.copyOfRange(args, 1, args.length);
		} else {
			guild = ((TextChannel) channel).getGuild();
		}

		if (rank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
			if (args.length > 0 && args[0].equalsIgnoreCase("reset")) {
				if (args.length > 1 && args[1].equalsIgnoreCase("yesimsure")) {
					return Template.get(channel, "command_config_reset_success");
				}
				GuildSettings.get(guild).reset();
				return Template.get(channel, "command_config_reset_warning");
			}
			if (args.length == 0) {
				Map<String, String> settings = GuildSettings.get(guild).getSettings();
				ArrayList<String> keys = new ArrayList<>(settings.keySet());
				Collections.sort(keys);
				String ret = "Current Settings for " + guild.getName() + Config.EOL + Config.EOL;
				ret += ":information_source: Settings indicated with a `*` are different from the default value";
				List<List<String>> data = new ArrayList<>();
				for (String key : keys) {
					if (DefaultGuildSettings.get(key).isReadOnly()) {
						if (!rank.isAtLeast(SimpleRank.BOT_ADMIN)) {
							continue;
						}
					}
					List<String> row = new ArrayList<>();
					String indicator = "";
					if (rank.isAtLeast(SimpleRank.BOT_ADMIN)) {
						indicator = DefaultGuildSettings.get(key).isReadOnly() ? "r" : " ";
					}
					indicator += settings.get(key).equals(DefaultGuildSettings.getDefault(key)) ? " " : "*";
					row.add(indicator + key);
					row.add(settings.get(key));
					row.add(DefaultGuildSettings.getDefault(key));
					data.add(row);
				}
				List<String> headers = new ArrayList<>();
				Collections.addAll(headers, "Setting name", "Current", "Default");
				ret += Misc.makeAsciiTable(headers,
						data, null);
				return ret;
			} else {
				if (!DefaultGuildSettings.isValidKey(args[0])) {
					return Template.get("command_config_key_not_exists");
				}
				if (DefaultGuildSettings.get(args[0]).isReadOnly() && !rank.isAtLeast(SimpleRank.BOT_ADMIN)) {
					return Template.get("command_config_key_read_only");
				}
				String newValue = "";
				if (args.length >= 2) {
					newValue = args[1];
					for (int i = 2; i < args.length; i++) {
						newValue += " " + args[i];
					}
					if (newValue.length() > 64) {
						newValue = newValue.substring(0, 64);
					}
				}
				if (args.length >= 2 && GuildSettings.get(guild).set(args[0], newValue)) {
					bot.clearChannels(guild);
					return Template.get("command_config_key_modified");
				}
				String tblContent = "";
				GuildSettings setting = GuildSettings.get(guild);
				for (String s : setting.getDescription(args[0])) {
					tblContent += s + Config.EOL;
				}
				return "Config help for **" + args[0] + "**" + Config.EOL + Config.EOL +
						"Current value: \"**" + GuildSettings.get(guild).getOrDefault(args[0]) + "**\"" + Config.EOL +
						"Default value: \"**" + setting.getDefaultValue(args[0]) + "**\"" + Config.EOL + Config.EOL +
						"Description: " + Config.EOL +
						Misc.makeTable(tblContent);
			}
		}
		return Template.get("command_config_no_permission");
	}
}