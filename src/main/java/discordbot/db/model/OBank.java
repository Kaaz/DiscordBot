package discordbot.db.model;

import discordbot.db.AbstractModel;

import java.sql.Timestamp;

/**
 * Created on 5-9-2016
 */
public class OBank extends AbstractModel {
	public int userId = 0;
	public int id = 0;
	public long currentBalance = 0L;
	public Timestamp createdOn = null;
}
