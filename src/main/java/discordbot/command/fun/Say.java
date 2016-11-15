package discordbot.command.fun;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

/**
 * !say
 * make the bot say something
 */
public class Say extends AbstractCommand {
	public Say() {
		super();
	}

	@Override
	public String getDescription() {
		return "repeats you";
	}

	@Override
	public String getCommand() {
		return "say";
	}

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public String[] getUsage() {
		return new String[]{"say <anything>"};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		if (args.length > 0) {
			return Joiner.on(" ").join(args);
		} else {
			return Template.get("command_say_whatexactly");
		}
	}
}