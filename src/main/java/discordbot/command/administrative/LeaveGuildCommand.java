package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.util.DisUtil;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

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
	public String execute(String[] args, MessageChannel channel, User author) {
		boolean shouldLeave = false;
		Guild guild = ((TextChannel) channel).getGuild();
		if (!bot.isAdmin(channel, author)) {
			return Template.get("no_permission");
		}
		if (bot.isCreator(author) && args.length >= 1 && args[0].matches("^\\d{10,}$")) {
			guild = bot.client.getGuildById(args[0]);
			if (guild == null) {
				return Template.get("cant_find_guild");
			}
			if (args.length == 1) {
				return "are you sure? :sob: type **`" + DisUtil.getCommandPrefix(channel) + "leaveguild " + args[0] + " confirm`** to leave _" + guild.getName() + "_";
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
			bot.out.sendAsyncMessage(bot.getDefaultChannel(guild), "This is goodbye :wave:", null);
			guild.getManager().leave();
			return ":+1:";
		}
		return ":face_palm: I expected you to know how to use it";
	}
}