package discordbot.util;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import discordbot.main.Config;
import discordbot.main.DiscordBot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Helper class to search for tracks on youtube
 */
public class YTSearch {
	private final YouTube youtube;
	private final YouTube.Search.List search;
	private final ConcurrentHashMap<String, SimpleResult> cache = new ConcurrentHashMap<>();
	private String apikey;

	public YTSearch(String apiKey) {
		this.apikey = apiKey;
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
		String queryName = query.trim().toLowerCase();
		if (cache.containsKey(queryName)) {
			return cache.get(queryName);
		}
		List<SimpleResult> results = getResults(query, 1);
		if (!results.isEmpty()) {
			cache.put(queryName, results.get(0));
			return results.get(0);
		}
		return null;
	}

	public List<SimpleResult> getPlayListItems(String playlistCode) {
		List<SimpleResult> playlist = new ArrayList<>();
		try {
			YouTube.PlaylistItems.List playlistRequest = youtube.playlistItems().list("id,contentDetails,snippet");
			playlistRequest.setKey(apikey);
			playlistRequest.setPlaylistId(playlistCode);
			playlistRequest.setFields("items(contentDetails/videoId,snippet/title,snippet/publishedAt),nextPageToken,pageInfo");
			String nextToken = "";
			do {
				playlistRequest.setPageToken(nextToken);
				PlaylistItemListResponse playlistItemResult = playlistRequest.execute();
				playlist.addAll(playlistItemResult.getItems().stream().map(playlistItem -> new SimpleResult(playlistItem.getContentDetails().getVideoId(), playlistItem.getSnippet().getTitle())).collect(Collectors.toList()));
				nextToken = playlistItemResult.getNextPageToken();
			} while (nextToken != null);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return playlist;
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