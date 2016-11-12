package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !blacklist/whitelist
 */
public class BlacklistCommand extends AbstractCommand {
	public BlacklistCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "blacklist/whitelist channels/users from interaction with the bot" + Config.EOL + Config.EOL +
				"Whitelist mode:" + Config.EOL +
				"Only interact with channels/users on the whitelist" + Config.EOL + Config.EOL +
				"blacklist mode:" + Config.EOL +
				"Don't interact with users/channels on this list";
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
				"bl channel                              //lists whitelisted/blacklisted channels ",
				"bl user                                 //lists whitelisted/blacklisted users ",
				"bl mode blacklist user                  //treat the user list as a blacklist",
				"bl mode whitelist user                  //treat the user list as a whitelist",
				"bl mode blacklist channel               //treat the channel list as a blacklist",
				"bl mode whitelist channel               //treat the channel list as a whitelist",
				"bl channel <add/remove> <channelname>   //Adds or removes a channel from the blacklist",
				"bl user <add/remove> <mention>          //adds/removes user from the blacklist",
				"bl mode disable                         //don't use the lists  ",
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

		return Template.get("command_invalid_use");
	}
}