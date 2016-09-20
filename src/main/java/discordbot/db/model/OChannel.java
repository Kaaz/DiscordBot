package discordbot.db.model;

import discordbot.db.AbstractModel;

public class OChannel extends AbstractModel {
	public int id = 0;
	public String discord_id = "";
	public String name = "";
	public int server_id = 0;
}
