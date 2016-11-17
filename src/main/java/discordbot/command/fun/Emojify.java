package discordbot.command.fun;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.util.Emojibet;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

/**
 * !emojify
 */
public class Emojify extends AbstractCommand {
	public static final int MAX_SIZE = 200;

	public Emojify() {
		super();
	}

	@Override
	public String getDescription() {
		return "turns everything what you say into emotes emotes";
	}

	@Override
	public String getCommand() {
		return "emojify";
	}

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"emojify <anything>",
				"example emojify hello world"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"emotify"
		};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		if (args.length > 0) {
			String combined = Joiner.on(" ").join(args);
			int strlen = combined.length();
			if (combined.length() > MAX_SIZE) {
				return Template.get("command_emojify_max_exceeded", MAX_SIZE);
			}
			StringBuilder output = new StringBuilder();
			for (int i = 0; i < strlen; i++) {
				output.append(Emojibet.getEmoji(combined.charAt(i)));
				output.append("\u200B");
			}
			return output.toString();
		} else {
			return Template.get("command_say_whatexactly");
		}
	}
}