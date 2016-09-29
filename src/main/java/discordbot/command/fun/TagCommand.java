package discordbot.command.fun;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !tag
 */
public class TagCommand extends AbstractCommand {

	public TagCommand(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Tags!";
	}

	@Override
	public String getCommand() {
		return "tag";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
		};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		return Template.get("not_implemented");
	}
}