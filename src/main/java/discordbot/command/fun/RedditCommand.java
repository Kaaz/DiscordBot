package discordbot.command.fun;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.modules.reddit.RedditScraper;
import discordbot.modules.reddit.pojo.Image;
import discordbot.modules.reddit.pojo.ImagePreview;
import discordbot.modules.reddit.pojo.Post;
import org.apache.commons.lang3.StringEscapeUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * !r
 * show something from reddit :)
 */
public class RedditCommand extends AbstractCommand {

	private static final Set<String> whitelistedDomains = new HashSet<String>(Arrays.asList(new String[]{
			"imgur.com",
			"i.imgur.com",
			"i.redd.it",
			"pbs.twimg.com",
			"gfycat.com",
			"file1.answcdn.com",
			"i.reddituploads.com"
	}));

	public RedditCommand(DiscordBot b) {
		super(b);
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
	public String execute(String[] args, IChannel channel, IUser author) {
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
					channel.sendFile(in, post.data.id + ".jpg", post.data.title);
					return "";
				} catch (IOException | MissingPermissionsException | DiscordException | RateLimitException e) {
					e.printStackTrace();
				}
			}
		}
		return Template.get("command_reddit_nothing");
	}
}