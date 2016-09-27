package discordbot.command.informative;

import discordbot.core.AbstractCommand;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;

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
			BufferedImage result = new BufferedImage(
					600, 600,
					BufferedImage.TYPE_INT_ARGB);
			Graphics g = result.getGraphics();
			URLConnection connection = new URL(author.getAvatarURL()).openConnection();
			connection.setRequestProperty("User-Agent", "bot emily-bot");
			BufferedImage profileImg = ImageIO.read(connection.getInputStream());
			BufferedImage backgroundImage = ImageIO.read(Launcher.class.getClassLoader().getResource("profile_bg_test.png"));
			g.drawImage(profileImg, 160, 80, 440, 380, 0, 0, profileImg.getWidth(), profileImg.getHeight(), null);
			g.drawImage(backgroundImage, 0, 0, 600, 600, 0, 0, 600, 600, null);
			File file = new File("profile_" + author.getID() + ".png");
			ImageIO.write(result, "png", file);
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