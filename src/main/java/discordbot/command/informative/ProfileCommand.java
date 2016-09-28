package discordbot.command.informative;

import discordbot.core.AbstractCommand;
import discordbot.main.DiscordBot;
import discordbot.modules.profile.ProfileImageV1;
import discordbot.modules.profile.ProfileImageV2;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;

/**
 * Profile command
 */
public class ProfileCommand extends AbstractCommand {
	public ProfileCommand(DiscordBot b) {
		super(b);
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
		return new String[]{"profile"};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		try {
			File file;
			if (args.length > 0 && args[0].equals("v1")) {
				ProfileImageV1 version1 = new ProfileImageV1(author);
				file = version1.getProfileImage();
			} else {
				ProfileImageV2 version2 = new ProfileImageV2(author);
				file = version2.getProfileImage();
			}
			channel.sendFile(file);
			file.delete();

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

	private String mark(String s, String mark) {
		return mark + s + mark;
	}
}