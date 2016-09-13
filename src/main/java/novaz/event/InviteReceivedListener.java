package novaz.event;

import novaz.core.AbstractEventListener;
import novaz.main.Config;
import novaz.main.NovaBot;
import sx.blah.discord.handle.impl.events.InviteReceivedEvent;
import sx.blah.discord.handle.impl.obj.Invite;
import sx.blah.discord.handle.obj.IInvite;

/**
 * Created on 30-8-2016
 */
public class InviteReceivedListener extends AbstractEventListener<InviteReceivedEvent> {
	public InviteReceivedListener(NovaBot novaBot) {
		super(novaBot);
	}

	@Override
	public boolean listenerIsActivated() {
		return false;
	}

	@Override
	public void handle(InviteReceivedEvent event) {
		IInvite[] invites = event.getInvites();
		try {
			for (IInvite invite : invites) {
				Invite.InviteResponse response = invite.details();
				event.getMessage().reply(String.format("Thank you for inviting me to join the guild guild **%s**!", response.getGuildName()));
				invite.accept();
				novaBot.out.sendMessage(novaBot.instance.getChannelByID(response.getChannelID()), String.format(
						"Hello all! %s invited me to join the **%s** guild. type %shelp to see what I can do.",
						event.getMessage().getAuthor().mention(),
						response.getGuildName(),
						Config.BOT_COMMAND_PREFIX));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
