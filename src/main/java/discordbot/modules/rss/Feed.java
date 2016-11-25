package discordbot.modules.rss;

import java.util.ArrayList;
import java.util.List;

public class Feed {

	private final String title;
	private final String link;
	private final String description;
	private final String language;
	private final String copyright;
	private final String pubDate;

	private final List<FeedMessage> entries = new ArrayList<>();

	public Feed(String title, String link, String description, String language,
				String copyright, String pubDate) {
		this.title = title;
		this.link = link;
		this.description = description;
		this.language = language;
		this.copyright = copyright;
		this.pubDate = pubDate;
	}

	public List<FeedMessage> getMessages() {
		return entries;
	}

	public String getTitle() {
		return title;
	}

	public String getLink() {
		return link;
	}

	public String getDescription() {
		return description;
	}

	public String getLanguage() {
		return language;
	}

	public String getCopyright() {
		return copyright;
	}

	public String getPubDate() {
		return pubDate;
	}

	@Override
	public String toString() {
		return "Feed [copyright=" + copyright + ", description=" + description
				+ ", language=" + language + ", link=" + link + ", pubDate="
				+ pubDate + ", title=" + title + "]";
	}

}