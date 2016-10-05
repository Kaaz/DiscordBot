package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.util.DisUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

/**
 * leaves the guild
 */
public class LeaveGuildCommand extends AbstractCommand {
	public LeaveGuildCommand(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "leaves guild :(";
	}

	@Override
	public String getCommand() {
		return "leaveguild";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"leaveguild     //leaves the guild"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		boolean shouldLeave = false;
		IGuild guild = channel.getGuild();
		if (!bot.isAdmin(channel, author)) {
			return Template.get("no_permission");
		}
		if (bot.isCreator(author) && args.length >= 1 && args[0].matches("^\\d{10,}$")) {
			guild = bot.client.getGuildByID(args[1]);
			if (guild == null) {
				return Template.get("cant_find_guild");
			}
			if (args.length == 1) {
				return "are you sure? :sob: type **" + DisUtil.getCommandPrefix(channel) + "leaveguild confirm** to leave";
			}
			if (args[1].equals("confirm")) {
				shouldLeave = true;
			}
		}
		if (args.length == 0) {
			return "are you sure? :sob: type **" + DisUtil.getCommandPrefix(channel) + "leaveguild confirm** to leave";
		}
		if (args[0].equals("confirm")) {
			shouldLeave = true;
		}
		if (shouldLeave) {
			try {
				bot.out.sendMessage(channel, "This is goodbye :wave:");
				channel.getGuild().leaveGuild();
			} catch (DiscordException | RateLimitException e) {
				e.printStackTrace();
			}
		}
		return ":face_palm: I expected you to know how to use it";
	}
}