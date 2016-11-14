package discordbot.db.model;

import discordbot.db.AbstractModel;

public class OMusic extends AbstractModel {
	public int id = 0;
	public String youtubecode = "";
	public String filename = "";
	public String youtubeTitle = "";
	public String artist = "";
	public long lastplaydate = 0;
	public int banned = 0;
	public String title = "";
	public int playCount = 0;
	public long lastManualPlaydate = 0L;
}