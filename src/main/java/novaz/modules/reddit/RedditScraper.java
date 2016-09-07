package novaz.modules.reddit;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import novaz.modules.reddit.pojo.*;
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
	private final static Gson gson = new GsonBuilder().
			registerTypeAdapter(CommentData.class, new CommentDataDeserializer()).
			excludeFieldsWithoutExposeAnnotation().
			create();

	public static List<Post> search(String subreddit, String arguments) {

		String response = doRequest(RedditConstants.URL + RedditConstants.SUBREDDIT_INDICATOR + subreddit + RedditConstants.SEARCH_PAGE + arguments);
		InitialData listing = gson.fromJson(response, InitialData.class);
		if (listing.data.children != null) {
			return listing.data.children;
		}
		return new ArrayList<>();
	}

	public static List<?> getComments(String id) {
		String response = doRequest(RedditConstants.URL + "comments/" + id + ".json");
		System.out.println(response);
		//[{},{}] first element is about post, 2nd is with comments
		List<InitialDataComment> initialData = gson.fromJson(response, new TypeToken<ArrayList<InitialDataComment>>() {
		}.getType());
		for (InitialDataComment initialDataComment : initialData) {
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			for (Comment child : initialDataComment.data.children) {
				System.out.println(child.data.author);
				System.out.println(child.data.body);
				System.out.println(child.data);

				System.out.println(child.data.created);
				System.out.println(child.data.id);
			}
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
