package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CBlacklistCommand;
import discordbot.db.controllers.CGuild;
import discordbot.handler.CommandHandler;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

/**
 * !blacklist/whitelist
 */
public class BlacklistCommand extends AbstractCommand {
	public BlacklistCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return
				"blacklist commands, so that they can't be used";
	}

	@Override
	public boolean isBlacklistable() {
		return false;
	}

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public String getCommand() {
		return "blacklist";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"bl command <command> [enable/disable]      //enables or disables commands",
				"",
				"example:",
				"bl command meme disable                    //this disabled the meme command"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"bl", "wl", "whitelist"
		};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		SimpleRank rank = bot.security.getSimpleRank(author, channel);
		if (!rank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
			return Template.get("no_permission");
		}
		int guildId = CGuild.getCachedId(channel);
		if (args.length == 0) {
			return "overview of blacklisted stuff goes here";
		}

		switch (args[0].toLowerCase()) {
			case "command":
				if (args.length < 2) {

					return "The following commands are blacklisted: " + Config.EOL + Config.EOL +
							":poop: :poop:";
				}
				AbstractCommand command = CommandHandler.getCommand(args[1].toLowerCase());
				if (command == null) {
					return Template.get("command_blacklist_command_not_found", args[1]);
				}
				if (!command.isBlacklistable()) {
					return Template.get("command_blacklist_not_blacklistable", args[1]);
				}
				if (args.length < 3) {
					if (CBlacklistCommand.find(guildId, command.getCommand()) != null) {
						return Template.get("command_blacklist_command_disabled", command.getCommand());
					}
					return Template.get("command_blacklist_command_enabled", command.getCommand());
				}
				if (args.length >= 3) {
					boolean disable = args[2].equalsIgnoreCase("disable");
					if (disable) {
						CBlacklistCommand.insertOrUpdate(guildId, command.getCommand());
						return Template.get("command_blacklist_command_disabled", command.getCommand());
					}
					CBlacklistCommand.delete(guildId, command.getCommand());
					return Template.get("command_blacklist_command_enabled", command.getCommand());
				}

				return String.format("Do stuff with the `%s` command", command.getCommand());
		}
		return Template.get("command_invalid_use");
	}
}