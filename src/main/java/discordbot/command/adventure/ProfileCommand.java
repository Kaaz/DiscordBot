package discordbot.command.adventure;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.modules.profile.ProfileImageV1;
import discordbot.modules.profile.ProfileImageV3;
import discordbot.util.DisUtil;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

import java.io.File;

/**
 * Profile command
 */
public class ProfileCommand extends AbstractCommand {
	public ProfileCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Shows your profile in a fancy way";
	}

	@Override
	public String getCommand() {
		return "profile";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"profile",
				"profile <@user>  //shows the profile of @user"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"avatar"
		};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		User user = author;
		if (args.length > 0) {
			if (DisUtil.isUserMention(args[0])) {
				user = bot.client.getUserById(DisUtil.mentionToId(args[0]));
			} else {
				user = DisUtil.findUserIn((TextChannel) channel, Joiner.on(" ").join(args).toLowerCase());
			}
			if (user == null) {
				return Template.get("cant_find_user", args[0]);
			}
		}
		try {
			File file;
			if (args.length > 0 && args[0].equals("v1")) {
				ProfileImageV1 version1 = new ProfileImageV1(user);
				file = version1.getProfileImage();
			} else {
				ProfileImageV3 version2 = new ProfileImageV3(user);
				file = version2.getProfileImage();
			}
			channel.sendFileAsync(file, null, message -> file.delete());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.getStackTrace();
			return "Error in creating image :(";
		}
		return "";
	}

	@Override
	public boolean isListed() {
		return false;
	}
}