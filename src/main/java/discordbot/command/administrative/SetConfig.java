package discordbot.command.administrative;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.guildsettings.DefaultGuildSettings;
import discordbot.handler.GuildSettings;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.Misc;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.util.*;

/**
 * !config
 * gets/sets the configuration of the bot
 */
public class SetConfig extends AbstractCommand {
	public SetConfig(DiscordBot b) {
		super(b);
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
	public String execute(String[] args, IChannel channel, IUser author) {
		IGuild guild;
		if (bot.isCreator(author) && args.length >= 1 && args[0].matches("^\\d{10,}$")) {
			guild = bot.client.getGuildByID(args[0]);
			if (guild == null) {
				return Template.get("command_confg_cant_find_guild");
			}
			args = Arrays.copyOfRange(args, 1, args.length);
		} else {
			guild = channel.getGuild();
		}
		int count = args.length;
		if (bot.isAdmin(channel, author)) {
			if (count == 0) {
				Map<String, String> settings = GuildSettings.get(guild).getSettings();
				ArrayList<String> keys = new ArrayList<>(settings.keySet());
				Collections.sort(keys);
				String ret = "Current Settings for " + guild.getName() + Config.EOL;
				List<List<String>> data = new ArrayList<>();
				for (String key : keys) {
					List<String> row = new ArrayList<>();
					row.add(key);
					row.add(settings.get(key));
					row.add(DefaultGuildSettings.getDefault(key));
					data.add(row);
				}
				List<String> headers = new ArrayList<>();
				Collections.addAll(headers, "Setting name", "Current", "Default");
				ret += Misc.makeAsciiTable(headers,
						data);
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