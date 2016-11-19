package discordbot.util;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import discordbot.main.Config;
import discordbot.main.DiscordBot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author John Grosh (jagrosh)
 */
public class YTSearch {
	private final YouTube youtube;
	private final YouTube.Search.List search;

	public YTSearch(String apiKey) {
		youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), (HttpRequest request) -> {
		}).setApplicationName(Config.BOT_NAME).build();
		YouTube.Search.List tmp = null;
		try {
			tmp = youtube.search().list("id,snippet");
			tmp.setOrder("relevance");
			tmp.setVideoCategoryId("10");
		} catch (IOException ex) {
			DiscordBot.LOGGER.error("Failed to initialize search: " + ex.toString());
		}
		search = tmp;
		if (search != null) {
			search.setKey(apiKey);
			search.setType("video");
			search.setFields("items(id/kind,id/videoId,snippet/title)");
		}
	}

	public SimpleResult getResults(String query) {
		List<SimpleResult> results = getResults(query, 1);
		if (!results.isEmpty()) {
			return results.get(0);
		}
		return null;
	}

	public List<SimpleResult> getResults(String query, int numresults) {
		List<SimpleResult> urls = new ArrayList<>();
		search.setQ(query);
		search.setMaxResults((long) numresults);

		SearchListResponse searchResponse;
		try {
			searchResponse = search.execute();
			List<SearchResult> searchResultList = searchResponse.getItems();
			searchResultList.forEach((sr) -> urls.add(new SimpleResult(sr.getId().getVideoId(), sr.getSnippet().getTitle())));
		} catch (IOException ex) {
			DiscordBot.LOGGER.error("YTSearch failure: " + ex.toString());
			return null;
		}
		return urls;
	}

	public class SimpleResult {
		private final String code;
		private final String title;

		public SimpleResult(String code, String title) {
			this.code = code;
			this.title = title;
		}

		public String getTitle() {
			return title;
		}

		public String getCode() {
			return code;
		}
	}
}