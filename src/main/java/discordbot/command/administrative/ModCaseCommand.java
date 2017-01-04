package discordbot.command.administrative;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class ModCaseCommand extends AbstractCommand {
	public ModCaseCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Modcases";
	}

	@Override
	public String getCommand() {
		return "modcase";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"kick <user>            //kicks user"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"case"
		};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		SimpleRank rank = bot.security.getSimpleRank(author);
		return "Not finished yet!";
//		guild.getController().kick(guild.getMember(author)).queue();
//		return Emojibet.OKE_SIGN;
	}
}