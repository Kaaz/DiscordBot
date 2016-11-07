package discordbot.command.administrative;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CGuild;
import discordbot.guildsettings.DefaultGuildSettings;
import discordbot.handler.GuildSettings;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.Misc;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

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
				"config <property> <value> //sets property"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"setting"
		};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		Guild guild;
		if (bot.isCreator(author) && args.length >= 1 && (args[0].matches("^\\d{10,}$") || args[0].matches("i\\d+"))) {
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
		int count = args.length;
		if (bot.isAdmin(channel, author)) {
			if (count == 0) {
				Map<String, String> settings = GuildSettings.get(guild).getSettings();
				ArrayList<String> keys = new ArrayList<>(settings.keySet());
				Collections.sort(keys);
				String ret = "Current Settings for " + guild.getName() + Config.EOL;
				ret += Config.EOL + "\\* means different from default";
				List<List<String>> data = new ArrayList<>();
				for (String key : keys) {
					List<String> row = new ArrayList<>();
					String different = settings.get(key).equals(DefaultGuildSettings.getDefault(key)) ? " " : "*";
					row.add(different + key);
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
				if (args[0].equalsIgnoreCase("autoupdate")) {
					Config.BOT_AUTO_UPDATE = Boolean.parseBoolean(args[1]);
					return "AutoUpdate set to " + Config.BOT_AUTO_UPDATE;
				}
				if (!DefaultGuildSettings.isValidKey(args[0])) {
					return Template.get("command_config_key_not_exists");
				}
				if (count >= 2 && GuildSettings.get(guild).set(args[0], args[1])) {
					bot.reloadGuild(guild);
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