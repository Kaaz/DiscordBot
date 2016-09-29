package discordbot.db.model;

import discordbot.db.AbstractModel;

import java.sql.Timestamp;

public class OTag extends AbstractModel {
	public int id = 0;
	public int guildId = 0;
	public String tagname = "";
	public String response = "";
	public int userId = 0;
	public Timestamp created = null;
}
