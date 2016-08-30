package novaz.event;

import novaz.core.AbstractEventListener;
import novaz.main.NovaBot;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;

/**
 * The bot recieves a message
 */
public class MessageReceivedListener extends AbstractEventListener<MessageReceivedEvent> {
	public MessageReceivedListener(NovaBot novaBot) {
		super(novaBot);
	}

	@Override
	public boolean listenerIsActivated() {
		return true;
	}

	@Override
	public void handle(MessageReceivedEvent event) {
		IMessage message = event.getMessage();
		if (event.getMessage().getChannel() instanceof IPrivateChannel) {
			novaBot.handlePrivateMessage((IPrivateChannel) message.getChannel(), message.getAuthor(), message);
		} else {
			novaBot.handleMessage(message.getGuild(), message.getChannel(), message.getAuthor(), message);
		}
	}
}