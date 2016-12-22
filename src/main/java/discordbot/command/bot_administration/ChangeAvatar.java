package discordbot.command.bot_administration;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.io.IOException;

/**
 * !avatar
 * manage avatar
 */
public class ChangeAvatar extends AbstractCommand {
	public ChangeAvatar() {
		super();
	}

	@Override
	public String getDescription() {
		return "Changes my avatar";
	}

	@Override
	public String getCommand() {
		return "updateavatar";
	}

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		SimpleRank rank = bot.security.getSimpleRank(author);

		if (!rank.isAtLeast(SimpleRank.CREATOR)) {
			return Template.get(channel, "command_no_permission");
		}
		if (args.length <= 1) {
			try {
				channel.getJDA().getSelfUser().getManager().setAvatar(Icon.from(Unirest.get(args[0]).asBinary().getBody())).queue();
			} catch (IOException | UnirestException e) {
				return "Error: " + e.getMessage();
			}
			return ":+1:";
		}
		return ":face_palm: I expected you to know how to use it";
	}
}