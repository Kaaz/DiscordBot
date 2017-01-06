package discordbot.command;

public interface ICommandReactionListener<T> {

	CommandReactionListener<T> getReactionListener(String InvokerUserId, T initialData);
}
