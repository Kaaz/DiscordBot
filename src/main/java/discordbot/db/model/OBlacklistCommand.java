package discordbot.db.model;

import discordbot.db.AbstractModel;

public class OBlacklistCommand extends AbstractModel {
	public int guildId = 0;
	public String command = "";
	public String channelId = "";
	public boolean blacklisted = false;
}
