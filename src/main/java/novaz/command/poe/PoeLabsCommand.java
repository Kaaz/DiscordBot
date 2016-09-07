package novaz.command.poe;

import novaz.core.AbstractCommand;
import novaz.main.NovaBot;
import novaz.modules.reddit.RedditScraper;
import novaz.modules.reddit.pojo.Comment;
import novaz.modules.reddit.pojo.Post;
import novaz.util.Misc;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;
import java.util.regex.Pattern;

/**
 * !poeitem
 * Analyzes an item from path of exile
 */
public class PoeLabsCommand extends AbstractCommand {
	Pattern p = Pattern.compile("YOUR_REGEX", Pattern.CASE_INSENSITIVE);

	public PoeLabsCommand(NovaBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Attempts to find a description from reddit for the labratory instance.";
	}

	@Override
	public String getCommand() {
		return "poelab";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"poelab              //lists for all difficulties",
				"poelab <difficulty> //only for that difficulty",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		List<Post> search = RedditScraper.search("pathofexile", "title%3ADaily+Labyrinth+author%3AAutoModerator&sort=new&restrict_sr=on&t=day");
		if (!search.isEmpty()) {
			Post post = search.get(0);
			List<Comment> comments = RedditScraper.getComments(post.data.getId());
			for (Comment comment : comments) {
				if (comment.data.isOp) {
					continue;
				}
				String searchText = comment.data.body.toLowerCase();
				if (args.length > 0) {
					if (searchText.contains(args[0].toLowerCase())) {
						return Misc.makeTable(comment.data.body);
					}
				}
				if (searchText.contains("normal") && searchText.contains("cruel") && searchText.contains("merciless")) {
					return Misc.makeTable(comment.data.body);
				}
			}
		}
		return "Can't find labdetails :(";
	}
}