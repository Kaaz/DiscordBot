package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.DisUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * leaves the guild
 */
public class LeaveGuildCommand extends AbstractCommand {
	public LeaveGuildCommand() {
		super();
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
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		boolean shouldLeave = false;
		Guild guild = ((TextChannel) channel).getGuild();
		SimpleRank rank = bot.security.getSimpleRank(author, channel);
		if (!rank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
			return Template.get("no_permission");
		}
		if (rank.isAtLeast(SimpleRank.BOT_ADMIN) && args.length >= 1 && args[0].matches("^\\d{10,}$")) {
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
			guild.leave();
			return ":+1:";
		}
		return ":face_palm: I expected you to know how to use it";
	}
}