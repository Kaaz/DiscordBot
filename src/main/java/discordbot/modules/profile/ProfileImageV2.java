package discordbot.modules.profile;

import discordbot.main.Launcher;
import discordbot.util.GfxUtil;
import sx.blah.discord.handle.obj.IUser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Created on 28-9-2016
 */
public class ProfileImageV2 extends ProfileImage {
	private Random rng;

	public ProfileImageV2(IUser user) {
		super(user);
		rng = new Random();
	}

	public File getProfileImage() throws IOException {
		int fontsize = 28;
		if (getUser().getName().length() <= 4) {
			fontsize = 32;
		}
		if (getUser().getName().length() > 8) {
			fontsize = 22;
		}
		Font defaultFont = new Font("Forte", Font.BOLD + Font.ITALIC, fontsize);
		Font score = new Font("Forte", Font.BOLD, 24);
		Font creditFont = new Font("Forte", Font.ITALIC, 12);
		BufferedImage result = new BufferedImage(
				320, 265,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) result.getGraphics();
		g.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		BufferedImage profileImg = getUserAvatar();
		BufferedImage backgroundImage = ImageIO.read(Launcher.class.getClassLoader().getResource("profile_bg_5.png"));

		g.drawImage(profileImg, 18, 33, 141, 159, 0, 0, profileImg.getWidth(), profileImg.getHeight(), null);
		g.drawImage(backgroundImage, 0, 0, 320, 265, 0, 0, 320, 265, null);

		GfxUtil.addShadow(getUser().getName(), defaultFont, 148, 89 + (fontsize / 2), g, Color.black);
		GfxUtil.addText(getUser().getName(), defaultFont, 148, 89 + (fontsize / 2), g, Color.white);
		GfxUtil.addRightText("made by Emily", creditFont, 318, 199, g, new Color(0x3A3A38));

		GfxUtil.addCenterText("" + rng.nextInt(100), score, 31, 246, g, new Color(0x5c7e32));
		GfxUtil.addCenterText("" + rng.nextInt(100), score, 134, 246, g, new Color(0x5c7e32));
		GfxUtil.addCenterText("" + rng.nextInt(100), score, 237, 246, g, new Color(0x5c7e32));
		File file = new File("profile_v2_" + getUser().getID() + ".png");
		ImageIO.write(result, "png", file);
		return file;
	}
}
