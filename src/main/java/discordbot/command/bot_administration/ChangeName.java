package discordbot.command.bot_administration;

import com.google.common.base.Joiner;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

/**
 * !changename
 * changes the bots name
 */
public class ChangeName extends AbstractCommand {
	public ChangeName() {
		super();
	}

	@Override
	public String getDescription() {
		return "Changes my name";
	}

	@Override
	public String getCommand() {
		return "changename";
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		SimpleRank rank = bot.security.getSimpleRank(author);
		if (!rank.isAtLeast(SimpleRank.CREATOR)) {
			return Template.get(channel, "command_no_permission");
		}
		if (args.length > 0) {
			bot.setUserName(Joiner.on(" ").join(args));
			return "You can call me **" + Joiner.on(" ").join(args) + "** from now :smile:";
		}
		return ":face_palm: I expected you to know how to use it";
	}
}