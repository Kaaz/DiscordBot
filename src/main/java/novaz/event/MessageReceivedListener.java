package novaz.event;

import novaz.core.AbstractEventListener;
import novaz.main.NovaBot;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

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
		IUser author = message.getAuthor();
		IChannel channel = message.getChannel();
//		String text = message.getContent();
//		author.getName();
//		channel.getName();
//		channel.getID();
		System.out.println(String.format("[%s][%s #%s][%s] %s", channel.getGuild().getName(), channel.getName(), channel.getID(), author.getName(), message.getContent()));
		try {
			new MessageBuilder(event.getClient()).withChannel(event.getMessage().getChannel()).withContent(event.getMessage().getContent()).build();
		} catch (RateLimitException | DiscordException | MissingPermissionsException e) {
			e.printStackTrace();
		}

	}
}