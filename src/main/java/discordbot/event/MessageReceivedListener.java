package discordbot.event;

import discordbot.core.AbstractEventListener;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;

/**
 * The bot recieves a message
 */
public class MessageReceivedListener extends AbstractEventListener<MessageReceivedEvent> {
	public MessageReceivedListener(DiscordBot discordBot) {
		super(discordBot);
	}

	@Override
	public boolean listenerIsActivated() {
		return true;
	}

	@Override
	public void handle(MessageReceivedEvent event) {
		IMessage message = event.getMessage();
		if (event.getMessage().getChannel() instanceof IPrivateChannel) {
			discordBot.handlePrivateMessage((IPrivateChannel) message.getChannel(), message.getAuthor(), message);
		} else {
			discordBot.handleMessage(message.getGuild(), message.getChannel(), message.getAuthor(), message);
		}
	}
}