package novaz.service;

import novaz.core.AbstractService;
import novaz.main.Config;
import novaz.main.NovaBot;
import novaz.modules.github.GitHub;
import novaz.modules.github.GithubConstants;
import novaz.modules.github.pojo.RepositoryCommit;
import novaz.util.TimeUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * check for news on github
 */
public class GithubService extends AbstractService {

	private final SimpleDateFormat exportDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final int MAX_COMMITS_PER_POST = 10;

	public GithubService(NovaBot b) {
		super(b);
	}

	@Override
	public String getIdentifier() {
		return "bot_code_updates";
	}

	@Override
	public long getDelayBetweenRuns() {
		return 10_000;
//		return 900_000;
	}

	@Override
	public boolean shouldIRun() {
		return true;
	}

	@Override
	public void beforeRun() {
		System.out.println("before running");
	}

	@Override
	public void run() {
		String commitsMessage = "";
		long lastKnownCommitTimestamp = Long.parseLong("0" + getData("last_date"));
		long newLastKnownCommitTimestamp = lastKnownCommitTimestamp;
		RepositoryCommit[] changesSinceHash = GitHub.getChangesSinceTimestamp("MaikWezinkhof", "discordbot", lastKnownCommitTimestamp);
		int commitCount = 0;//probably changesSinceHash.length - 1
		for (int i = changesSinceHash.length - 1; i >= 0; i--) {
			RepositoryCommit commit = changesSinceHash[i];
			Long timestamp = 0L;
			try {
				timestamp = GithubConstants.githubDate.parse(commit.getCommit().getCommitterShort().getDate()).getTime();
			} catch (ParseException ignored) {
			}
			String message = commit.getCommit().getMessage();
			String committer = commit.getCommit().getCommitterShort().getName();
			if (timestamp > lastKnownCommitTimestamp) {
				commitsMessage += commitOutputFormat(timestamp, message, committer, commit.getSha());
				newLastKnownCommitTimestamp = timestamp;
				commitCount++;
				if (commitCount >= MAX_COMMITS_PER_POST) {
					break;
				}
			}
		}
//		saveData("last_date", newLastKnownCommitTimestamp);
		System.out.println(commitsMessage);
	}

	@Override
	public void afterRun() {
		System.out.println("after running");
	}

	private String commitOutputFormat(Long timestamp, String message, String committer, String sha) {
		String sb = ":new::arrow_up: `" + sha.substring(0, 7) + "` :bust_in_silhouette:**"+committer+"** :timer: " + TimeUtil.getRelativeTime(timestamp / 1000L, false) + Config.EOL;
		sb += message + Config.EOL;
		return sb;
	}
}
