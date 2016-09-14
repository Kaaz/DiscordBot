package novaz.command.fun;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.Config;
import novaz.main.NovaBot;
import novaz.modules.reddit.RedditScraper;
import novaz.modules.reddit.pojo.Image;
import novaz.modules.reddit.pojo.ImagePreview;
import novaz.modules.reddit.pojo.Post;
import org.apache.commons.lang3.StringEscapeUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Random;

/**
 * !r
 * show something from reddit :)
 */
public class RedditCommand extends AbstractCommand {
	public RedditCommand(NovaBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Posts something from reddit";
	}

	@Override
	public String getCommand() {
		return "r";
	}

	@Override
	public String[] getUsage() {
		return new String[]{"r <subreddit>"};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		String subReddit = "funny";
		if (args.length > 0) {
			subReddit = args[0];
		}
		List<Post> dailyTop = RedditScraper.getDailyTop(subReddit);
		if (dailyTop.isEmpty()) {
			return TextHandler.get("command_reddit_sub_not_found");
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
			if (post.data.domain.equals("imgur.com")) {
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
		return TextHandler.get("command_reddit_nothing");
	}
}