package discordbot.db.model;

import discordbot.db.AbstractModel;

/**
 * Created on 10-8-2016
 */
public class OGuild extends AbstractModel {
	public int id = 0;
	public String discord_id = "";
	public String name = "";
	public int owner = 0;
	public int active = 0;
	public int banned = 0;

	public boolean isBanned() {
		return banned == 1;
	}
}
