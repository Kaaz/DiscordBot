package discordbot.command.administrative;

import discordbot.command.administrative.modactions.AbstractModActionCommand;
import discordbot.db.model.OModerationCase;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;

public class TempBanCommand extends AbstractModActionCommand {
	@Override
	public String getDescription() {
		return "Bans a user for a while";
	}

	@Override
	public String getCommand() {
		return "tempban";
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	protected OModerationCase.PunishType getPunishType() {
		return OModerationCase.PunishType.TMP_BAN;
	}

	@Override
	protected Permission getRequiredPermission() {
		return Permission.BAN_MEMBERS;
	}

	@Override
	protected boolean punish(Guild guild, Member member) {
		guild.getController().kick(member).queue();
		return true;
	}
}