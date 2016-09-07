package novaz.modules.reddit;


import com.google.gson.Gson;
import novaz.modules.reddit.gson.Child;
import novaz.modules.reddit.gson.CommentsResult;
import novaz.modules.reddit.gson.SearchResult;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.apache.http.protocol.HTTP.USER_AGENT;

public class RedditScraper {
	final static Gson gson = new Gson();

	public static List<Child> search(String subreddit, String arguments) {
		String response = doRequest(RedditConstants.URL + RedditConstants.SUBREDDIT_INDICATOR + subreddit + RedditConstants.SEARCH_PAGE + arguments);
		SearchResult searchResult = gson.fromJson(response, SearchResult.class);
		List<Child> children = searchResult.getSearchResultData().getChildren();
		if (children != null) {
			return children;
		}
		return new ArrayList<Child>();
	}

	public static List<Child> getComments(String id) {
		String response = doRequest(RedditConstants.URL + "comments/" + id + ".json");
		System.out.println(response);
		//[{},{}] first element is about post, 2nd is with comments
		CommentsResult[] searchResult = gson.fromJson(response, CommentsResult[].class);
		if (searchResult.length >= 2) {
			return searchResult[1].getData().getChildren();
		}
		return new ArrayList<>();
	}

	private static String doRequest(String url) {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		request.addHeader("User-Agent", USER_AGENT);
		try {
			HttpResponse response = client.execute(request);
			BufferedReader rd = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent()));

			StringBuilder result = new StringBuilder();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			return result.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}


}
