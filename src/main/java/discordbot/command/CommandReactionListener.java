package discordbot.command;

import net.dv8tion.jda.core.entities.Message;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Consumer;

public class CommandReactionListener<DataType> {

	private final LinkedHashMap<String, Consumer<Message>> reactions;
	private volatile DataType data;

	public CommandReactionListener(DataType data) {
		this.data = data;
		reactions = new LinkedHashMap<>();
	}

	public boolean hasReaction(String emote) {
		return reactions.containsKey(emote);
	}

	public void react(String emote, Message message) {
		reactions.get(emote).accept(message);
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

	public Set<String> getEmotes() {
		return reactions.keySet();
	}
}
