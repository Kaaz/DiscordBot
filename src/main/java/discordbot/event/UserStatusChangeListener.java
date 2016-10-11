package discordbot.event;

import discordbot.core.AbstractEventListener;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.impl.events.StatusChangeEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.MessageList;

public class UserStatusChangeListener extends AbstractEventListener<StatusChangeEvent> {
	private String[] specialGuilds;

	public UserStatusChangeListener(DiscordBot discordBot) {
		super(discordBot);
		specialGuilds = new String[]{
				"225168913808228352",
				"180818466847064065"
		};
	}

	@Override
	public boolean listenerIsActivated() {
		return true;
	}

	@Override
	public void handle(StatusChangeEvent event) {
		IUser user = event.getUser();
		if (!user.isBot() || user.equals(discordBot.client.getOurUser())) {
			return;
		}
		Status status = event.getNewStatus();
		if (status.getStatusMessage() != null && 	status.getStatusMessage().equals(discordBot.client.getOurUser().getStatus().getStatusMessage())) {
			for (String specialGuild : specialGuilds) {
				IGuild guild = discordBot.client.getGuildByID(specialGuild);
				if (guild == null) {
					continue;
				}
				IUser guildUser = guild.getUserByID(user.getID());
				if (guildUser != null) {
					IChannel defaultChannel = discordBot.getDefaultChannel(guild);
					for (IMessage message : new MessageList(discordBot.client, defaultChannel, 25)) {
						if (message.getAuthor().equals(discordBot.client.getOurUser())) {
							return;
						}
					}
					discordBot.out.sendMessage(defaultChannel, "Oh hey look " + user.mention() + " we have the same status :joy:");
				}

			}
		}
	}
}