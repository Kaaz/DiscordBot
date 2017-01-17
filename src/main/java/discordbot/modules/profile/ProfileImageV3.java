package discordbot.modules.profile;

import discordbot.db.controllers.CUser;
import discordbot.db.model.OUser;
import discordbot.main.Launcher;
import discordbot.util.GfxUtil;
import net.dv8tion.jda.core.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class ProfileImageV3 extends ProfileImage {


	public ProfileImageV3(User user) {
		super(user);
	}

	public File getProfileImage() throws IOException {
		Random rng = new Random(Long.parseLong(getUser().getId()));
		int fontSize;
		if (getUser().getName().length() <= 4) {
			fontSize = 32;
		} else if (getUser().getName().length() < 12) {
			fontSize = 22;
		} else if (getUser().getName().length() < 25) {
			fontSize = 18;
		} else {
			fontSize = 14;
		}
		OUser dbuser = CUser.findBy(getUser().getId());
		double level = Math.log(dbuser.commandsUsed + 1);//+1 for this command
		int xpPercent = (int) ((level % 1D) * 100D);
		int skillPoints = (int) level + 3;
		int health = rng.nextInt(skillPoints);
		int attack = rng.nextInt(skillPoints - health);
		int defense = skillPoints - health - attack;
		Font defaultFont = new Font("Forte", Font.BOLD + Font.ITALIC, fontSize);
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
		BufferedImage backgroundImage = ImageIO.read(Launcher.class.getClassLoader().getResource("profile_bg_6.png"));
		BufferedImage xpProgressBar = ImageIO.read(Launcher.class.getClassLoader().getResource("progressbar.png"));

		g.drawImage(profileImg, 18, 33, 141, 159, 0, 0, profileImg.getWidth(), profileImg.getHeight(), null);
		g.drawImage(backgroundImage, 0, 0, 320, 265, 0, 0, 320, 265, null);
		g.drawImage(xpProgressBar, 137, 133, 317 - (int) ((181D / 100D) * (100D - xpPercent)), 148, 0, 0, 175, 15, null);

		GfxUtil.addCenterShadow(getUser().getName(), defaultFont, 222, 71 + (fontSize / 2), g, Color.black);
		GfxUtil.addCenterText(getUser().getName(), defaultFont, 222, 71 + (fontSize / 2), g, Color.white);
		GfxUtil.addRightText("made by Emily", creditFont, 318, 199, g, new Color(0x3A3A38));
		GfxUtil.addCenterShadow("" + xpPercent + "%", creditFont, 218, 145, g, Color.black);
		GfxUtil.addCenterText("" + xpPercent + "%", creditFont, 218, 145, g, new Color(0xf37000));//% xp

		GfxUtil.addText("" + (int) level, score, 173, 118, g, new Color(0xffff00));//rewards
//		GfxUtil.addRightText("" + rng.nextInt(1000), score, 290, 118, g, new Color(0x36cbe9));//currency

		GfxUtil.addCenterText("" + health, score, 31, 246, g, new Color(0x5c7e32));//health
		GfxUtil.addCenterText("" + attack, score, 134, 246, g, new Color(0x5c7e32));//attack
		GfxUtil.addCenterText("" + defense, score, 237, 246, g, new Color(0x5c7e32));//defense
//		int w = 3;
//		for (int i = 0; i <= 100; i++) {
//			g.setColor(GfxUtil.getThreatLevel(i / 100d));
//			g.fillRect(i * w, 0, w, 200);
//		}
		File file = new File("profile_v3_" + getUser().getId() + ".png");
		ImageIO.write(result, "png", file);
		return file;
	}
}
