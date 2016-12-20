package discordbot.service;

import discordbot.core.AbstractService;
import discordbot.main.BotContainer;
import discordbot.main.Config;
import discordbot.modules.github.GitHub;
import discordbot.modules.github.GithubConstants;
import discordbot.modules.github.pojo.RepositoryCommit;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * check for news on github
 */
public class GithubService extends AbstractService {

	private final static int MAX_COMMITS_PER_POST = 10;
	private final static String gitUser = "Kaaz";
	private final static String gitRepo = "discordbot";
	private final static String commitUrl = "https://github.com/%s/%s/commit/%s";

	public GithubService(BotContainer b) {
		super(b);
	}

	@Override
	public String getIdentifier() {
		return "bot_code_updates";
	}

	@Override
	public long getDelayBetweenRuns() {
		return 900_000;
	}

	@Override
	public boolean shouldIRun() {
		return true;
	}

	@Override
	public void beforeRun() {
	}

	@Override
	public void run() {
		long lastKnownCommitTimestamp = Long.parseLong("0" + getData("last_date"));
		long newLastKnownCommitTimestamp = lastKnownCommitTimestamp;
		RepositoryCommit[] changesSinceHash = GitHub.getChangesSinceTimestamp(gitUser, gitRepo, lastKnownCommitTimestamp);
		int commitCount = 0;//probably changesSinceHash.length - 1
		LinkedHashMap<String, String> commitMap = new LinkedHashMap<>();
		String committerName = "??";
		String committerAvatar = "";
		String committerUrl = "";

		for (int i = changesSinceHash.length - 1; i >= 0; i--) {
			RepositoryCommit commit = changesSinceHash[i];
			Long timestamp = 0L;
			try {
				timestamp = GithubConstants.githubDate.parse(commit.getCommit().getCommitterShort().getDate()).getTime();
			} catch (ParseException ignored) {
			}
			String message = commit.getCommit().getMessage();
			committerName = commit.getAuthor().getLogin();
			committerUrl = "https://github.com/" + commit.getAuthor().getLogin();
			committerAvatar = commit.getAuthor().getAvatarUrl();
			if (timestamp > lastKnownCommitTimestamp) {
//				commitsMessage += commitOutputFormat(timestamp, message, committer, commit.getSha());
				newLastKnownCommitTimestamp = timestamp;
				commitCount++;
				if (commitCount >= MAX_COMMITS_PER_POST) {
					break;
				}
				commitMap.put(commit.getSha(), message);
			}
		}
		if (commitCount > 0) {
			EmbedBuilder embed = new EmbedBuilder();
			embed.setColor(new Color(0x4FA1FF));
			embed.setAuthor(committerName, committerUrl, committerAvatar);
			embed.setTitle("Changes to my code");
			String description = "There have been **" + commitCount + "** commits to my code " + Config.EOL + Config.EOL;
			description += "** Hash**          **Description**" + Config.EOL;
			int maxCharsPerline = 65;
			for (Map.Entry<String, String> entry : commitMap.entrySet()) {
				String cmt = String.format("[`%s`](" + commitUrl + ")", entry.getKey().substring(0, 7), gitUser, gitRepo, entry.getKey());

				List<String> strings = Arrays.asList(entry.getValue().split("\\r?\\n", 0));
				boolean first = true;
				for (String commitLine : strings) {
					List<String> subCommitLine = new ArrayList<>();
					while (!commitLine.isEmpty()) {
						if (commitLine.length() <= maxCharsPerline) {
							subCommitLine.add(commitLine.trim());
							commitLine = "";
						} else {
							int index = commitLine.lastIndexOf(" ", maxCharsPerline);
							if (index == -1) {
								index = maxCharsPerline;
							}
							subCommitLine.add(commitLine.substring(0, index).trim());
							commitLine = commitLine.substring(index);
						}
					}

					for (String s : subCommitLine) {
						if (first) {
							first = false;
							description += cmt + "     " + s + Config.EOL;
						} else {
							description += "`.......`     " + s + Config.EOL;
						}
					}
				}
				description += Config.EOL;
			}
			embed.setDescription(description);
			for (TextChannel chan : getSubscribedChannels()) {
				sendTo(chan, embed.build());
			}
		}
		saveData("last_date", newLastKnownCommitTimestamp);
	}

	@Override
	public void afterRun() {
	}
}