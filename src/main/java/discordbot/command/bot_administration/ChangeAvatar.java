package discordbot.command.bot_administration;

import discordbot.core.AbstractCommand;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

/**
 * !avatar
 * manage avatar
 */
public class ChangeAvatar extends AbstractCommand {
	public ChangeAvatar(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Changes my avatar";
	}

	@Override
	public String getCommand() {
		return "updateavatar";
	}

	@Override
	public boolean isListed() {
		return false;
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
		if (args.length <= 1) {
			return "Disabled for now :(";
		}
		return ":face_palm: I expected you to know how to use it";
	}
}