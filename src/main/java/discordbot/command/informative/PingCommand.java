package discordbot.command.informative;

import discordbot.core.AbstractCommand;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !ping
 */
public class PingCommand extends AbstractCommand {
	public PingCommand() {
		super();
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
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		long now = System.currentTimeMillis();
		channel.sendMessage(":outbox_tray: checking ping").queue(
				message -> message.editMessage(":inbox_tray: ping is " + (System.currentTimeMillis() - now) + "ms").queue()
		);
		return "";
	}
}