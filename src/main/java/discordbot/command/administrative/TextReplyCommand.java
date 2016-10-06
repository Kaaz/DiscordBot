package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.Misc;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * managing text replies for the bot
 */
public class TextReplyCommand extends AbstractCommand {
	Pattern pattern = null;

	public TextReplyCommand(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "make the bot reply to patterns";
	}

	@Override
	public String getCommand() {
		return "textreply";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"tr <tag> <regex> <response>"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"tr"
		};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (!bot.isCreator(author)) {
			return Template.get("no_permission");
		}
		if (args.length == 0) {
			return Template.get("command_invalid_use");
		}
		if (args.length >= 2) {
			String tag = args[1];//change to reply_pattern table
			switch (args[0].toLowerCase()) {
				case "regex":
					try {
						pattern = Pattern.compile(args[1]);
					} catch (PatternSyntaxException exception) {
						return exception.getDescription() + Config.EOL + Misc.makeTable(exception.getMessage());
					}
					return "Your regex is :+1:";
				case "response":
					return Template.get("not_yet_implemented");
				case "tag":
					return Template.get("not_yet_implemented");
				case "cd":
					return Template.get("not_yet_implemented");
				case "create":
					return Template.get("not_yet_implemented");
				default:
					return Template.get("invalid_use");
			}
		}
		return Template.get("invalid_use");
	}
}