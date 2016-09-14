package novaz.command.administrative;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

import java.util.EnumSet;

/**
 * !purge
 * Purges messages in channel
 */
public class PurgeComand extends AbstractCommand {
	public PurgeComand(NovaBot b) {
		super(b);
	}

	@Override
	public boolean isAllowedInPrivateChannel() {
		return false;
	}

	@Override
	public String getDescription() {
		return "purges messages";
	}

	@Override
	public String getCommand() {
		return "purge";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"purge       //deletes last 100 messages",
				"purge @user //deletes messages from user",
				"purge nova  //deletes my messages :("
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		EnumSet<Permissions> permissions = channel.getModifiedPermissions(bot.instance.getOurUser());
		if (args.length == 0) {
			boolean hasManageMessages = permissions.contains(Permissions.MANAGE_MESSAGES);
			channel.getMessages().stream().filter(msg -> !msg.isPinned()).forEach(
					msg -> {
						System.out.println(msg.getAuthor().getName() + ": " + msg.getContent());
						if (hasManageMessages) {

							bot.out.deleteMessage(msg);
						} else {
							if (msg.getAuthor().equals(bot.instance.getOurUser())) {
								bot.out.deleteMessage(msg);
							}
						}
					});
			if (hasManageMessages) {
				return TextHandler.get("command_purge_success");
			}
			return TextHandler.get("permission_missing_manage_messages");
		}
		return TextHandler.get("command_invalid_use");
	}
}