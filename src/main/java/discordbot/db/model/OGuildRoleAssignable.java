package discordbot.db.model;

import discordbot.db.AbstractModel;

public class OGuildRoleAssignable extends AbstractModel {
	public int guildId = 0;
	public String discordRoleId = "";
	public String description = "";
	public String roleName = "";
}
