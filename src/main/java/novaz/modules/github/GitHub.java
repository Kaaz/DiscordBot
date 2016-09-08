package novaz.modules.github;

import com.google.gson.Gson;
import novaz.modules.github.pojo.RepositoryCommit;
import novaz.util.HttpHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created on 8-9-2016
 */
public class GitHub {
	//Thu, 05 Jul 2012 15:31:30 GMT
//	private static final SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	private static final SimpleDateFormat lastModifiedFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
	private static final Gson gson = new Gson();

	public static void getChangesSinceLastTime() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.add(Calendar.DAY_OF_MONTH, -1);
		System.out.println(lastModifiedFormat.format(cal.getTime()));
		String response = HttpHelper.doRequest(GithubConstants.getCommitEndPoint("MaikWezinkhof", "discordbot","ed9fc1d0e95911f1ee3c70abf9611197ae574680"));
		RepositoryCommit[] repositoryCommit = gson.fromJson(response, RepositoryCommit[].class);
		for (RepositoryCommit commit : repositoryCommit) {
			System.out.println(commit.getSha());
			System.out.println(commit.getCommit().getMessage());
			System.out.println(commit.getCommit().getCommitterShort().getDate());
		}

	}
}
