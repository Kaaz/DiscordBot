package novaz.command.poe;

import novaz.core.AbstractCommand;
import novaz.main.NovaBot;
import novaz.modules.reddit.RedditScraper;
import novaz.modules.reddit.gson.Child;
import novaz.util.Misc;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

/**
 * !poeitem
 * Analyzes an item from path of exile
 */
public class PoeLabsCommand extends AbstractCommand {
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
		List<Child> search = RedditScraper.search("pathofexile", "title%3ADaily+Labyrinth+author%3AAutoModerator&sort=new&restrict_sr=on&t=day");
		if (!search.isEmpty()) {
			Child post = search.get(0);
			List<Child> comments = RedditScraper.getComments(post.getData().getId());
			for (Child comment : comments) {
				String searchText = comment.getData().getBody().toLowerCase();
				if (args.length > 0) {
					if (searchText.contains(args[0].toLowerCase())) {
						return Misc.makeTable(comment.getData().getBody());
					}
				}
				if (searchText.contains("normal") && searchText.contains("cruel") && searchText.contains("merciless")) {
					return Misc.makeTable(comment.getData().getBody());
				}
			}
		}
		return "Can't find labdetails :(";
	}
}