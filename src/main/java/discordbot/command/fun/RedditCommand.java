package discordbot.command.fun;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.modules.reddit.RedditScraper;
import discordbot.modules.reddit.pojo.Image;
import discordbot.modules.reddit.pojo.ImagePreview;
import discordbot.modules.reddit.pojo.Post;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * !r
 * show something from reddit :)
 */
public class RedditCommand extends AbstractCommand {

	private static final Set<String> whitelistedDomains = new HashSet<>(Arrays.asList(new String[]{
			"imgur.com",
			"i.imgur.com",
			"i.redd.it",
			"pbs.twimg.com",
			"gfycat.com",
			"file1.answcdn.com",
			"i.reddituploads.com"
	}));

	public RedditCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Posts something from reddit";
	}

	@Override
	public String getCommand() {
		return "reddit";
	}

	@Override
	public String[] getUsage() {
		return new String[]{"r <subreddit>"};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"r"
		};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		String subReddit = "funny";
		if (args.length > 0) {
			subReddit = args[0];
		}
		List<Post> dailyTop = RedditScraper.getDailyTop(subReddit);
		if (dailyTop.isEmpty()) {
			return Template.get("command_reddit_sub_not_found");
		}
		Random rng = new Random();
		Post post;
		do {
			int index = rng.nextInt(dailyTop.size());
			post = dailyTop.get(index);
			dailyTop.remove(index);
			if (post.data.is_self) {
				break;
			}
			if (whitelistedDomains.contains(post.data.domain)) {
				break;
			}
		} while (dailyTop.size() > 0);
		if (post.data.is_self) {
			return ":newspaper:" + Config.EOL + post.data.getTitle() + Config.EOL + post.data.getSelftext();
		}
		ImagePreview preview = post.data.getPreview();
		if (preview.images.size() > 0) {
			for (Image image : preview.images) {
				try (InputStream in = new URL(StringEscapeUtils.unescapeHtml4(image.source.url)).openStream()) {
					File outputfile = new File("tmp_" + channel.getId() + ".jpg");
					ImageIO.write(ImageIO.read(in), "jpg", outputfile);
					channel.sendFile(outputfile, new MessageBuilder().append(post.data.title).build()).queue(message -> outputfile.delete());
					return "";
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return Template.get("command_reddit_nothing");
	}
}