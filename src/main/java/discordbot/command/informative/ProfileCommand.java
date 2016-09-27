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
			Font defaultFont = new Font("SansSerif", Font.BOLD, 36);
			Font creditFont = new Font("comissans", Font.ITALIC, 20);
			BufferedImage result = new BufferedImage(
					645, 265,
					BufferedImage.TYPE_INT_ARGB);
			Graphics g = result.getGraphics();
			URLConnection connection = new URL(author.getAvatarURL()).openConnection();
			connection.setRequestProperty("User-Agent", "bot emily-bot");
			BufferedImage profileImg;
			try {
				profileImg = ImageIO.read(connection.getInputStream());
			} catch (Exception ignored) {
				profileImg = ImageIO.read(Launcher.class.getClassLoader().getResource("default_profile.jpg"));
			}
			BufferedImage backgroundImage = ImageIO.read(Launcher.class.getClassLoader().getResource("profile_bg_test_2.png"));
			g.drawImage(profileImg, 68, 30, 195, 155, 0, 0, profileImg.getWidth(), profileImg.getHeight(), null);
			g.drawImage(backgroundImage, 0, 0, 645, 265, 0, 0, 645, 265, null);
			addText(author.getName(), defaultFont, 70, 180, g, Color.black);
			addText("made by Emily", creditFont, 506, 260, g, new Color(0xFFE2F7));
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

	private void addText(String text, Font font, int x, int y, Graphics g, Color color) {
		g.setFont(font);
		g.setColor(color);
		g.drawString(text, x, y);
	}

	@Override
	public boolean isListed() {
		return false;
	}

	private String mark(String s, String mark) {
		return mark + s + mark;
	}
}