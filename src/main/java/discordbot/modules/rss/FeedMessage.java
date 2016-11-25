package discordbot.modules.rss;

import discordbot.main.Config;

public class FeedMessage {

	private String title;
	private String description;
	private String link;
	private String author;
	private String guid;
	private String thumbnail;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	@Override
	public String toString() {
		return "FeedMessage [" + Config.EOL +
				"   title=" + title + Config.EOL +
				"   thumbnail=" + thumbnail + Config.EOL +
				"   description=" + description + Config.EOL +
				"   link=" + link + Config.EOL +
				"   author=" + author + Config.EOL +
				"   guid=" + guid + Config.EOL +
				"]";
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
}