package discordbot.handler.discord;


import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

public class RoleModifyTask {

	private final User user;
	private final Role role;
	private final boolean add;

	public RoleModifyTask(User user, Role role, boolean add) {

		this.user = user;
		this.role = role;
		this.add = add;
	}

	public boolean isAdd() {
		return add;
	}

	public Role getRole() {
		return role;
	}

	public User getUser() {
		return user;
	}
}
