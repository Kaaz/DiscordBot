package discordbot.command.informative;

import discordbot.core.AbstractCommand;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

/**
 * !ping
 */
public class PingCommand extends AbstractCommand {
	public PingCommand(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "checks the latency of the bot";
	}

	@Override
	public String getCommand() {
		return "ping";
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
		long now = System.currentTimeMillis();
		bot.out.sendAsyncMessage(channel, ":outbox_tray: checking ping", message -> message.updateMessageAsync(":inbox_tray: ping is " + (System.currentTimeMillis() - now) + "ms", null));
		return "";
	}
}