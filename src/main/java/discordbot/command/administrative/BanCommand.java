package discordbot.command.administrative;

import discordbot.command.administrative.modactions.AbstractModActionCommand;
import discordbot.db.model.OModerationCase;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;

public class BanCommand extends AbstractModActionCommand {
	@Override
	public String getDescription() {
		return "bans a member from your guild";
	}

	@Override
	public String getCommand() {
		return "ban";
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	protected OModerationCase.PunishType getPunishType() {
		return OModerationCase.PunishType.BAN;
	}

	@Override
	protected Permission getRequiredPermission() {
		return Permission.BAN_MEMBERS;
	}

	@Override
	protected boolean punish(Guild guild, Member member) {
		guild.getController().ban(member, 7);
		return true;
	}
}