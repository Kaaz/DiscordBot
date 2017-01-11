package discordbot.command.administrative;

import discordbot.command.administrative.modactions.AbstractModActionCommand;
import discordbot.db.model.OModerationCase;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;

public class WarnCommand extends AbstractModActionCommand {
	@Override
	public String getDescription() {
		return "Give a user a warning";
	}

	@Override
	public String getCommand() {
		return "warn";
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	protected OModerationCase.PunishType getPunishType() {
		return OModerationCase.PunishType.WARN;
	}

	@Override
	protected Permission getRequiredPermission() {
		return Permission.KICK_MEMBERS;
	}

	@Override
	protected boolean punish(Guild guild, Member member) {
		return true;
	}
}