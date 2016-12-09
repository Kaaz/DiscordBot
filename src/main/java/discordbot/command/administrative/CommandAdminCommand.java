package discordbot.command.administrative;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CBlacklistCommand;
import discordbot.db.controllers.CGuild;
import discordbot.db.model.OBlacklistCommand;
import discordbot.handler.CommandHandler;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.DisUtil;
import discordbot.util.Emojibet;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

import java.util.List;
import java.util.TreeMap;

/**
 * !disable/enable commands per guild/channel
 */
public class CommandAdminCommand extends AbstractCommand {
	public CommandAdminCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Commands can be enabled/disabled through this command." + Config.EOL +
				"A channel specific setting will always override the guild setting";
	}

	@Override
	public boolean canBeDisabled() {
		return false;
	}

	@Override
	public boolean isListed() {
		return true;
	}

	@Override
	public String getCommand() {
		return "commandadmin";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"ca <command> [enable/disable]               //enables/disables commands in the whole guild",
				"ca <command> [enable/disable] [#channel]    //enables/disables commands in a channel. This overrides the above",
				"ca resetchannel [#channel]                  //resets the overrides for a channel",
				"ca command [command]                        //resets the overrides for a channel",
				"ca resetallchannels                         //resets the overrides for all channels",
				"ca reset yesimsure                          //enables all commands + resets overrides",
				"",
				"examples:",
				"ca meme disable                             //this disabled the meme command",
				"ca meme enable #spam                        //overrides and meme is enabled in #spam"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"ca"
		};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		SimpleRank rank = bot.security.getSimpleRank(author, channel);
		if (!rank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
			return Template.get("no_permission");
		}
		int guildId = CGuild.getCachedId(channel);
		if (args.length == 0) {
			TreeMap<String, List<String>> map = new TreeMap<>();
			List<OBlacklistCommand> blacklist = CBlacklistCommand.getBlacklistedFor(guildId);
			if (blacklist.isEmpty()) {
				return Template.get("command_blacklist_command_empty");
			}
			StringBuilder ret = new StringBuilder().append("The following commands are restricted: ").append(Config.EOL).append(Config.EOL);
			String lastCommand = blacklist.get(0).command;
			boolean guildwide = false;
			boolean firstSubItem = true;
			for (OBlacklistCommand item : blacklist) {
				String icon = item.blacklisted ? Emojibet.NO_ENTRY : Emojibet.OKE_SIGN;
				String cmdStatus = item.blacklisted ? "disabled" : "enabled";
				if (!lastCommand.equals(item.command)) {
					lastCommand = item.command;
					ret.append(Config.EOL).append(Config.EOL);
					guildwide = false;
					firstSubItem = true;
				}
				if (item.channelId.equals("0")) {
					ret.append(icon).append(" `").append(item.command).append("` is ").append(cmdStatus).append(" guild-wide!").append(Config.EOL);
					guildwide = true;
				} else {
					TextChannel tmp = bot.client.getTextChannelById(item.channelId);
					if (tmp == null) {
						continue;
					}
					if (!guildwide && firstSubItem) {
						ret.append("`").append(item.command).append("` is ").append(cmdStatus).append(" in: ");
					}
					if (!firstSubItem) {
						ret.append(" | ");
					}
					if (firstSubItem && guildwide) {
						ret.append("Except in: ");
					}
					firstSubItem = false;
					ret.append(tmp.getAsMention()).append(" ").append(icon);
				}
			}
			return ret.toString();
		}
		switch (args[0].toLowerCase()) {
			case "resetchannel":
				if (args.length != 2) {
					return Template.get("command_invalid_use");
				}
				String channelId = DisUtil.mentionToId(args[1]);
				TextChannel c = bot.client.getTextChannelById(channelId);
				if (c == null) {
					return Template.get("command_invalid_use");
				}
				CBlacklistCommand.deleteOverridesInChannel(guildId, channelId);
				CommandHandler.reloadBlackListFor(guildId);
				return Template.get("command_blacklist_reset_channel", c.getAsMention());
			case "resetallchannels":
				CBlacklistCommand.deleteAllOverrides(guildId);
				CommandHandler.reloadBlackListFor(guildId);
				return Template.get("command_blacklist_reset_all_channels");
			case "reset":
				if (args.length != 2) {
					return Template.get("command_invalid_use");
				}
				CBlacklistCommand.deleteGuild(guildId);
				CommandHandler.reloadBlackListFor(guildId);
				return Template.get("command_blacklist_reset");

		}
		if (args.length < 2) {
			return Template.get("command_invalid_use");
		}
		AbstractCommand command = CommandHandler.getCommand(args[0].toLowerCase());
		if (command == null) {
			return Template.get("command_blacklist_command_not_found", args[0]);
		}
		if (!command.canBeDisabled()) {
			return Template.get("command_blacklist_not_blacklistable", args[0]);
		}
		if (!args[1].equals("enable") && !args[1].equals("disable")) {
			return Template.get("command_invalid_use");
		}
		boolean blacklist = args[1].equals("disable");
		String channelId = "0";//guild-wide
		if (args.length > 2) {
			if (!DisUtil.isChannelMention(args[2])) {
				return Template.get("command_invalid_use");
			}
			channelId = DisUtil.mentionToId(args[2]);
			TextChannel c = bot.client.getTextChannelById(channelId);
			if (c == null) {
				return Template.get("command_invalid_use");
			}
		}
		if (blacklist) {
			CBlacklistCommand.insertOrUpdate(guildId, command.getCommand(), channelId, true);
			CommandHandler.reloadBlackListFor(guildId);
			return Template.get("command_blacklist_command_disabled", command.getCommand());
		} else {
			if (!channelId.equals("0")) {
				CBlacklistCommand.insertOrUpdate(guildId, command.getCommand(), channelId, false);
			} else {
				CBlacklistCommand.delete(guildId, command.getCommand(), channelId);
			}
			CommandHandler.reloadBlackListFor(guildId);
			return Template.get("command_blacklist_command_enabled", command.getCommand());
		}
	}
}