/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package discordbot.util;

import com.mashape.unirest.http.Unirest;
import discordbot.main.Launcher;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * some helpful methods when creating images
 */
public class GfxUtil {

    public static void addCenterText(String text, Font font, int x, int y, Graphics g, Color color) {
        g.setFont(font);
        g.setColor(color);
        g.drawString(text, Math.max(0, x - ((int) g.getFontMetrics().getStringBounds(text, g).getWidth() / 2)), y);
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

    /**
     * @param percentage 0.0 - 1.0
     * @return color from green to red
     */
    public static Color getThreatLevel(double percentage) {
        return Color.getHSBColor((float) (1f - percentage) * .35f, 1, 1);
    }

    /**
     * Returns the average color of an image
     *
     * @param url the url to get the image
     * @return average color OR fallback color in case of invalid url
     */
    public static Color getAverageColor(String url) {
        if (url == null) {
            return new Color(27, 137, 255);
        }
        try {
            BufferedImage img = ImageIO.read(Unirest.get(url).asBinary().getRawBody());
            int x0 = 0;
            int y0 = 0;
            int x1 = x0 + img.getWidth();
            int y1 = y0 + img.getHeight();
            long sumr = 0, sumg = 0, sumb = 0;
            for (int x = x0; x < x1; x++) {
                for (int y = y0; y < y1; y++) {
                    Color pixel = new Color(img.getRGB(x, y));
                    sumr += pixel.getRed();
                    sumg += pixel.getGreen();
                    sumb += pixel.getBlue();
                }
            }
            int num = img.getWidth() * img.getHeight();
            return new Color((int) sumr / num, (int) sumg / num, (int) sumb / num);
        } catch (Exception e) {
            Launcher.logToDiscord(e, "img-url", url);
            e.printStackTrace();
        }
        return new Color(27, 137, 255);
    }
}
