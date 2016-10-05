package discordbot.util;

import java.awt.*;

/**
 * some helpful methods when creating images
 */
public class GfxUtil {

	public static void addCenterText(String text, Font font, int x, int y, Graphics g, Color color) {
		g.setFont(font);
		g.setColor(color);
		int realX = Math.max(0, x - ((int) g.getFontMetrics().getStringBounds(text, g).getWidth() / 2));
		g.drawString(text, realX, y);
	}

	public static void addRightText(String text, Font font, int x, int y, Graphics g, Color color) {
		g.setFont(font);
		g.setColor(color);
		int realX = Math.max(0, x - ((int) g.getFontMetrics().getStringBounds(text, g).getWidth()));
		g.drawString(text, realX, y);
	}

	public static void addShadow(String text, Font font, int x, int y, Graphics g, Color color) {
		addText(text, font, x + 1, y + 1, g, color);
		addText(text, font, x + 1, y - 1, g, color);
		addText(text, font, x - 1, y + 1, g, color);
		addText(text, font, x - 1, y - 1, g, color);
	}

	public static void addCenterShadow(String text, Font font, int x, int y, Graphics g, Color color) {
		addCenterText(text, font, x + 1, y + 1, g, color);
		addCenterText(text, font, x + 1, y - 1, g, color);
		addCenterText(text, font, x - 1, y + 1, g, color);
		addCenterText(text, font, x - 1, y - 1, g, color);
	}

	public static void addText(String text, Font font, int x, int y, Graphics g, Color color) {
		g.setFont(font);
		g.setColor(color);
		g.drawString(text, x, y);
	}
}
