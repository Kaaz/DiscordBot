package discordbot.modules.github;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created on 8-9-2016
 */
public class GithubConstants {

	public static final SimpleDateFormat githubDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private static final String ENDPOINT = "https://api.github.com/";

	//username, repository
	private static final String COMMIT_ENDPOINT = ENDPOINT + "repos/%s/%s/commits";

	public static String getCommitEndPoint(String user, String repository, long timestamp) {
		if (timestamp <= 0L) {
			return getCommitEndPoint(user, repository);
		}
		return String.format(COMMIT_ENDPOINT, user, repository) + "?since=" + githubDate.format(new Date(timestamp));
	}

	private static String getCommitEndPoint(String user, String repository) {
		return String.format(COMMIT_ENDPOINT, user, repository);
	}
}
