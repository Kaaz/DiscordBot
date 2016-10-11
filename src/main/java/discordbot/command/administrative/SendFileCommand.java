package discordbot.command.administrative;

import com.google.common.base.Joiner;
import discordbot.core.AbstractCommand;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.io.File;
import java.io.IOException;

/**
 */
public class SendFileCommand extends AbstractCommand {
	public SendFileCommand(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "executes commandline stuff";
	}

	@Override
	public String getCommand() {
		return "sendfile";
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
	public String execute(String[] args, IChannel channel, IUser author) {
		if (!bot.isCreator(author)) {
			return ":upside_down: There's only one person who I trust enough to do that";
		}
		if (args.length == 0) {
			return ":face_palm: I expected you to know how to use it";
		}
		File f = new File(Joiner.on("").join(args));
		if (f.exists()) {
			try {
				channel.sendFile(f);
			} catch (IOException | MissingPermissionsException | RateLimitException | DiscordException e) {
				return e.toString();
			}
			return "";
		}
		return "File doesn't exist";
	}
}