package discordbot.db.model;

import discordbot.db.AbstractModel;

import java.sql.Timestamp;

public class OReplyPattern extends AbstractModel {
	public int userId = 0;
	public int id = 0;
	public int guildId = 0;
	public String tag = "";
	public String pattern = "";
	public String reply = "";
	public Timestamp createdOn = null;
	public long cooldown = 0;
}
