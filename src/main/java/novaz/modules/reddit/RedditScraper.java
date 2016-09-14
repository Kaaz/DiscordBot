package novaz.modules.reddit;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import novaz.modules.reddit.pojo.*;
import novaz.util.HttpHelper;

import java.util.ArrayList;
import java.util.List;

public class RedditScraper {
	private final static Gson gson = new GsonBuilder().
			registerTypeAdapter(CommentData.class, new CommentDataDeserializer()).
			excludeFieldsWithoutExposeAnnotation().
			create();

	public static List<Post> search(String subreddit, String arguments) {

		String response = HttpHelper.doRequest(RedditConstants.URL + RedditConstants.SUBREDDIT_INDICATOR + subreddit + RedditConstants.SEARCH_PAGE + arguments);
		InitialData listing = gson.fromJson(response, InitialData.class);
		if (listing.data.children != null) {
			return listing.data.children;
		}
		return new ArrayList<>();
	}

	public static List<Post> getDailyTop(String subreddit) {

		String response = HttpHelper.doRequest(RedditConstants.URL + RedditConstants.SUBREDDIT_INDICATOR + subreddit + "/top.json?sort=top&t=day");
		InitialData listing = gson.fromJson(response, InitialData.class);
		if (listing.data != null && listing.data.children != null) {
			return listing.data.children;
		}
		return new ArrayList<>();
	}

	public static List<Comment> getComments(String id) {
		String response = HttpHelper.doRequest(RedditConstants.URL + "comments/" + id + ".json");
		List<InitialDataComment> initialData = gson.fromJson(response, new TypeToken<ArrayList<InitialDataComment>>() {
		}.getType());
		List<Comment> commentList = new ArrayList<>();
		for (InitialDataComment initialDataComment : initialData) {
			commentList.addAll(initialDataComment.data.children);
		}
		return commentList;
	}
}
