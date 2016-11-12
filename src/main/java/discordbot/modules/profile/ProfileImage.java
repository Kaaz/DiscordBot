package discordbot.modules.profile;

import discordbot.main.Launcher;
import net.dv8tion.jda.core.entities.User;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created on 28-9-2016
 */
public abstract class ProfileImage {

	private final User user;

	public ProfileImage(User user) {
		this.user = user;
	}

	public BufferedImage getUserAvatar() throws IOException {
		URLConnection connection = new URL(getUser().getAvatarUrl()).openConnection();
		connection.setRequestProperty("User-Agent", "bot emily-bot");
		BufferedImage profileImg;
		try {
			profileImg = ImageIO.read(connection.getInputStream());
		} catch (Exception ignored) {
			profileImg = ImageIO.read(Launcher.class.getClassLoader().getResource("default_profile.jpg"));
		}
		return profileImg;
	}

	public User getUser() {
		return user;
	}
}
