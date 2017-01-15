package discordbot.db.model;

import discordbot.db.AbstractModel;

import java.sql.Timestamp;

public class OBet extends AbstractModel {
	public int id = 0;
	public int guildId = 0;
	public String title = "";
	public int ownerId = 0;
	public Timestamp createdOn = null;
	public Timestamp startedOn = null;
	public Timestamp endsAt = null;
	public int price = 0;
}
