package novaz.event;

import novaz.core.AbstractEventListener;
import novaz.main.NovaBot;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

/**
 * The bot recieves a message
 */
public class MessageReceivedListener extends AbstractEventListener<MessageReceivedEvent> {
	public MessageReceivedListener(NovaBot novaBot) {
		super(novaBot);
	}

	@Override
	public boolean listenerActivated() {
		return true;
	}

	@Override
	public void handle(MessageReceivedEvent event) {
		IMessage message = event.getMessage();
		novaBot.handleMessage(message.getGuild(), message.getChannel(), message.getAuthor(), message);
	}
}