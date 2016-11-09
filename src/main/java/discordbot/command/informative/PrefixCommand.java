package discordbot.command.informative;

import discordbot.core.AbstractCommand;
import discordbot.main.DiscordBot;
import discordbot.util.DisUtil;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

public class PrefixCommand extends AbstractCommand {
	public PrefixCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Forgot what the prefix is? I got you covered";
	}

	@Override
	public String getCommand() {
		return "prefix";
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
		return "My prefix on this guild is `" + DisUtil.getCommandPrefix(channel) + "`";
	}
}