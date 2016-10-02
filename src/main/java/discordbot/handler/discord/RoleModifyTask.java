package discordbot.handler.discord;

import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

public class RoleModifyTask {

	private final IUser user;
	private final IRole role;
	private final boolean add;

	public RoleModifyTask(IUser user, IRole role, boolean add) {

		this.user = user;
		this.role = role;
		this.add = add;
	}

	public boolean isAdd() {
		return add;
	}

	public IRole getRole() {
		return role;
	}

	public IUser getUser() {
		return user;
	}
}
