package discordbot.command.fun;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.DisUtil;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !say
 * make the bot say something
 */
public class SayCommand extends AbstractCommand {
	public SayCommand() {
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
			String output = Joiner.on(" ").join(args);
			if (DisUtil.isUserMention(output)) {
				if (bot.security.getSimpleRank(author, channel).isAtLeast(SimpleRank.GUILD_ADMIN)) {
					return output;
				}
				return Template.get("command_say_contains_mention");
			}
			return output;
		} else {
			return Template.get("command_say_whatexactly");
		}
	}
}