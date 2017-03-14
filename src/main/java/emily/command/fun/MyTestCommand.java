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

package emily.command.fun;

import emily.command.CommandVisibility;
import emily.core.AbstractCommand;
import emily.main.DiscordBot;
import emily.templates.Templates;
import emily.util.Misc;
import emily.util.TimeUtil;
import emily.util.YTUtil;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

public class MyTestCommand extends AbstractCommand {

    public MyTestCommand() {
        super();
    }

    @Override
    public boolean isListed() {
        return true;
    }

    @Override
    public String getDescription() {
        return "kaaz's test command";
    }

    @Override
    public String getCommand() {
        return "test";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "NOPE"

        };
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
//		String ret = "a xp test\n \n";
        String ret = "a\n \n";
        if (args.length >= 1) {
            return "in millis: " + TimeUnit.MILLISECONDS.toSeconds(TimeUtil.toMillis(Misc.joinStrings(args, 0))) + " seconds";
        }
//		for (int i = 1; i < 30; i++) {
//			ret += String.format("`level %02d | xp %5d`\n", i, GameUtil.getXpFor(i));
//		}
        if (args.length == 0) {
            for (int i = 1; i <= 50; i++) {
                bot.out.sendAsyncMessage(channel, "message \\#"+i);
            }
            TextChannel tx = (TextChannel) channel;
            return Templates.welcome_new_user.format((tx).getGuild().getId(), author, channel, args,tx.getGuild());
        }
        Matcher matcher = YTUtil.yturl.matcher(args[0]);
        if (matcher.find()) {
            ret += "Match! " + matcher.groupCount() + " groups: \n";
            for (int i = 0; i < matcher.groupCount(); i++) {
                ret += "group " + i + " " + matcher.group(i) + "\n";
            }
        }
        ret += "extracted code = " + YTUtil.extractCodeFromUrl(args[0]);
        ret += "\nextracted playlist = " + YTUtil.getPlayListCode(args[0]);
        return ret;
    }
//	@Override
//	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
//		BufferedImage result = new BufferedImage(1000, 600, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D g = (Graphics2D) result.getGraphics();
//		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//		int w = 10;
//		for (int i = 0; i <= 100; i++) {
//			g.setColor(GfxUtil.getThreatLevel(i / 100d));
//			g.fillRect(i * w, 0, w, 300);
//		}
//		for (int i = 0; i <= 100; i++) {
//			g.setColor(getColor(i));
//			g.fillRect(i * w, 300, w, 300);
//		}
//		File file = new File("test_" + author.getId() + ".png");
//		try {
//			ImageIO.write(result, "png", file);
//			channel.sendFile(file, null).queue(message -> file.delete(), throwable -> file.delete());
//			return "";
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		for (Guild guild : bot.client.getGuilds()) {
//			//apart from the double ;, it looks find,
//			guild.getPublicChannel().sendMessage(new EmbedBuilder().setColor(Color.cyan).addField("Test", "Test", false).build()).queue();
//		}
//		return "";//for the implementation see GameHandler
//	}

    private Color getColor(int percent) {
        if (percent <= 50) {
            return new Color(percent * 5, 255, 0);
        }
        return new Color(255, 510 - percent * 5, 0);
    }
}