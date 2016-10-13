package discordbot.command.bot_administration;

import com.google.common.base.Joiner;
import discordbot.core.AbstractCommand;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

/**
 * !changename
 * changes the bots name
 */
public class ChangeName extends AbstractCommand {
	public ChangeName(DiscordBot b) {
		super(b);
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
	public String execute(String[] args, MessageChannel channel, User author) {
		if (!bot.isCreator(author)) {
			return ":upside_down: There's only one person who I trust enough to do that";
		}
		if (args.length > 0) {
			bot.setUserName(Joiner.on(" ").join(args));
			return "You can call me **" + Joiner.on(" ").join(args) + "** from now :smile:";
		}
		return ":face_palm: I expected you to know how to use it";
	}
}