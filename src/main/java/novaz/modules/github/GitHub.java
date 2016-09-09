package novaz.modules.github;

import com.google.gson.Gson;
import novaz.modules.github.pojo.RepositoryCommit;
import novaz.util.HttpHelper;

import java.util.Calendar;

public class GitHub {
	private static final Gson gson = new Gson();

	/**
	 * Retrieves a list of changes since timestamp
	 *
	 * @param username   the github username
	 * @param repository the repository name
	 * @param timestamp  the starting timestamp
	 * @return a list of commits since timestamp
	 */
	public static RepositoryCommit[] getChangesSinceTimestamp(String username, String repository, long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.add(Calendar.DAY_OF_MONTH, -1);
		String response = HttpHelper.doRequest(GithubConstants.getCommitEndPoint(username, repository, timestamp));
		return gson.fromJson(response, RepositoryCommit[].class);
	}
}
