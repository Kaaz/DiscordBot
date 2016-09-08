package novaz.modules.github;

import java.text.SimpleDateFormat;

/**
 * Created on 8-9-2016
 */
class GithubConstants {
	private static final SimpleDateFormat lastModifiedFormat = new SimpleDateFormat("YYYY-MM-DD'T'HH:MM:SS'Z'");

	private static final String ENDPOINT = "https://api.github.com/";

	//username, repository
	private static final String COMMIT_ENDPOINT = ENDPOINT + "repos/%s/%s/commits";

	static String getCommitEndPoint(String user, String repository, String sha) {
		if (sha.length() < 32) {
			return getCommitEndPoint(user, repository);
		}
		return String.format(COMMIT_ENDPOINT, user, repository) + "?sha=" + sha;
	}

	static String getCommitEndPoint(String user, String repository) {
		return String.format(COMMIT_ENDPOINT, user, repository);
	}
}
