package novaz.db.model;

import novaz.db.AbstractModel;

import java.sql.Timestamp;

/**
 * Created on 5-9-2016
 */
public class OGuildMember extends AbstractModel {
	public int guildId = 0;
	public int userId = 0;
	public Timestamp joinDate = null;
}
