package discordbot.command.administrative;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.guildsettings.DefaultGuildSettings;
import discordbot.handler.GuildSettings;
import discordbot.handler.TextHandler;
import discordbot.main.Config;
import discordbot.main.NovaBot;
import discordbot.util.Misc;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * !config
 * gets/sets the configuration of the bot
 */
public class SetConfig extends AbstractCommand {
	public SetConfig(NovaBot b) {
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
				"config <property> <value> //sets property"};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		int count = args.length;
		if (bot.isOwner(channel, author)) {
			if (count == 0) {
				Map<String, String> settings = GuildSettings.get(channel.getGuild()).getSettings();
				Collections.sort(new ArrayList<>(settings.keySet()));
				String ret = "Current Settings for " + channel.getGuild().getName() + Config.EOL;
				List<List<String>> data = new ArrayList<>();
				for (Map.Entry<String, String> entry : settings.entrySet()) {
					List<String> row = new ArrayList<>();
					row.add(entry.getKey());
					row.add(entry.getValue());
					row.add(DefaultGuildSettings.getDefault(entry.getKey()));
					data.add(row);
				}
				List<String> headers = new ArrayList<>();
				Collections.addAll(headers, "Setting name", "Current", "Default");
				ret += Misc.makeAsciiTable(headers,
						data);
				return ret;
			} else {
				if (!DefaultGuildSettings.isValidKey(args[0])) {
					return TextHandler.get("command_config_key_not_exists");
				}
				if (count >= 2 && GuildSettings.get(channel.getGuild()).set(args[0], args[1])) {
					return TextHandler.get("command_config_key_modified");
				}
				String tblContent = "";
				GuildSettings setting = GuildSettings.get(channel.getGuild());
				for (String s : setting.getDescription(args[0])) {
					tblContent += s + Config.EOL;
				}
				return "Config help for **" + args[0] + "**" + Config.EOL + Config.EOL +
						"Current value: \"**" + GuildSettings.get(channel.getGuild()).getOrDefault(args[0]) + "**\"" + Config.EOL +
						"Default value: \"**" + setting.getDefaultValue(args[0]) + "**\"" + Config.EOL + Config.EOL +
						"Description: " + Config.EOL +
						Misc.makeTable(tblContent);
			}
		}
		return TextHandler.get("command_config_no_permission");
	}
}