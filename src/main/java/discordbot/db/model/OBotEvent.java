package discordbot.db.model;

import discordbot.db.AbstractModel;

import java.sql.Timestamp;

public class OBotEvent extends AbstractModel {
	public int id = 0;
	public Timestamp createdOn = null;
	public String group = "";
	public String subGroup = "";
	public String data = "";
}
