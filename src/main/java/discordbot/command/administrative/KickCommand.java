package discordbot.command.administrative;

import discordbot.command.administrative.modactions.AbstractModActionCommand;
import discordbot.db.model.OModerationCase;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;

/**
 * command for kicking users from a guild
 */
public class KickCommand extends AbstractModActionCommand {
	@Override
	public String getDescription() {
		return "Kicks a member from your guild";
	}

	@Override
	public String getCommand() {
		return "kick";
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	protected OModerationCase.PunishType getPunishType() {
		return OModerationCase.PunishType.KICK;
	}

	@Override
	protected Permission getRequiredPermission() {
		return Permission.KICK_MEMBERS;
	}

	@Override
	protected boolean punish(Guild guild, Member member) {
		guild.getController().kick(member).queue();
		return true;
	}
}