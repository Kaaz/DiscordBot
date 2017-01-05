package discordbot.command;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.HashMap;
import java.util.function.Consumer;

public class CommandReactionListener<DataType> {

	private final HashMap<String, Consumer<Message>> reactions;
	private volatile DataType data;

	public CommandReactionListener(DataType data) {
		this.data = data;
		reactions = new HashMap<>();
	}

	public DataType getData() {
		return data;
	}

	public void setData(DataType data) {
		this.data = data;
	}

	public void registerReaction(String emoji, Consumer<Message> consumer) {
		reactions.put(emoji, consumer);
	}

	public Consumer<Message> getCallback() {
		return message -> {
			if (message.getChannel() instanceof TextChannel) {
				TextChannel channel = (TextChannel) message.getChannel();

			}
		};
	}
}
