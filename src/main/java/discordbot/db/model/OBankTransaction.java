package discordbot.db.model;

import discordbot.db.AbstractModel;

import java.sql.Timestamp;

public class OBankTransaction extends AbstractModel {
	public int id = 0;
	public int bankFrom = 0;
	public int bankTo = 0;
	public Timestamp date = null;
	public String description = "";
	public int amount = 0;
}
