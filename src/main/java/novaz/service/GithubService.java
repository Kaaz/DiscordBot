package novaz.service;

import novaz.core.AbstractService;
import novaz.main.Config;
import novaz.main.NovaBot;
import novaz.modules.github.GitHub;
import novaz.modules.github.GithubConstants;
import novaz.modules.github.pojo.RepositoryCommit;
import novaz.util.Misc;
import novaz.util.TimeUtil;
import sx.blah.discord.handle.obj.IChannel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * check for news on github
 */
public class GithubService extends AbstractService {

	private final SimpleDateFormat exportDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final static int MAX_COMMITS_PER_POST = 25;

	public GithubService(NovaBot b) {
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
		String totalMessage;
		String commitsMessage = "";
		long lastKnownCommitTimestamp = Long.parseLong("0" + getData("last_date"));
		long newLastKnownCommitTimestamp = lastKnownCommitTimestamp;
		RepositoryCommit[] changesSinceHash = GitHub.getChangesSinceTimestamp("MaikWezinkhof", "discordbot", lastKnownCommitTimestamp);
		int commitCount = 0;//probably changesSinceHash.length - 1
		List<List<String>> tblContent = new ArrayList<>();
		for (int i = changesSinceHash.length - 1; i >= 0; i--) {
			List<String> tableRow = new ArrayList<>();
			RepositoryCommit commit = changesSinceHash[i];
			Long timestamp = 0L;
			try {
				timestamp = GithubConstants.githubDate.parse(commit.getCommit().getCommitterShort().getDate()).getTime();
			} catch (ParseException ignored) {
			}
			String message = commit.getCommit().getMessage();
			String committer = commit.getAuthor().getLogin();
			if (3026105 == commit.getAuthor().getId()) {
				committer = "Kaaz";
			}
			if (timestamp > lastKnownCommitTimestamp) {
				commitsMessage += commitOutputFormat(timestamp, message, committer, commit.getSha());

				newLastKnownCommitTimestamp = timestamp;
				commitCount++;
				if (commitCount >= MAX_COMMITS_PER_POST) {
					break;
				}
				tableRow.add(commit.getSha().substring(0, 7));
				tableRow.add(committer);
				tableRow.add(message);
				tblContent.add(tableRow);
			}
		}
		if (commitCount > 0) {
			if (commitCount == 1) {
				totalMessage = "There has been a commit to **" + Config.BOT_NAME + "**" + Config.EOL;
			} else {
				totalMessage = "There have been **" + commitCount + "** commits to **" + Config.BOT_NAME + "**" + Config.EOL;
			}
			if (commitCount < 3) {
				totalMessage += commitsMessage;
			} else {
				totalMessage += Misc.makeAsciiTable(Arrays.asList("hash", "by", "description"), tblContent);
			}
			for (IChannel iChannel : getSubscribedChannels()) {
				bot.sendMessage(iChannel, totalMessage);
			}
		}
		saveData("last_date", newLastKnownCommitTimestamp);
	}

	@Override
	public void afterRun() {
	}

	private String commitOutputFormat(Long timestamp, String message, String committer, String sha) {
		String timeString = "";
		long localtimestamp = timestamp + 1000 * 60 * 60 * 2;//+2hours cheat
		if (System.currentTimeMillis() - localtimestamp > 1000 * 60 * 60) {//only when its 1h+
			timeString = " :clock3: " + TimeUtil.getRelativeTime(localtimestamp / 1000L, false);
		}
		String sb = ":arrow_up: `" + sha.substring(0, 7) + "` " + " :bust_in_silhouette: " + committer + timeString + Config.EOL;
		sb += ":pencil: `" + message + "`" + Config.EOL;
		return sb;
	}
}