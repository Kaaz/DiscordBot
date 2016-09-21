package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.RateLimitException;

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
		return "avatar";
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
	public String execute(String[] args, IChannel channel, IUser author) {
		if (!bot.isCreator(author)) {
			return ":upside_down: There's only one person who I trust enough to do that";
		}
		if (args.length == 2) {
			try {
				bot.instance.changeAvatar(Image.forUrl(args[0], args[1]));
				return "How do you like my new look? :blush:";
			} catch (DiscordException | RateLimitException e) {
				e.printStackTrace();
				return "Couldn't do it because: " + e.getMessage();
			}
		}
		return ":face_palm: I expected you to know how to use it";
	}
}