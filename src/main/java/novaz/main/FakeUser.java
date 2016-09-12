package novaz.main;


import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.List;
import java.util.Optional;

public class FakeUser implements IUser {
	@Override
	public String getName() {
		return "fake1";
	}

	@Override
	public Status getStatus() {
		return null;
	}

	@Override
	public String getAvatar() {
		return null;
	}

	@Override
	public String getAvatarURL() {
		return null;
	}

	@Override
	public Presences getPresence() {
		return null;
	}

	@Override
	public String getDisplayName(IGuild guild) {
		return null;
	}

	@Override
	public String mention() {
		return "@"+getName();
	}

	@Override
	public String mention(boolean mentionWithNickname) {
		return null;
	}

	@Override
	public String getDiscriminator() {
		return null;
	}

	@Override
	public List<IRole> getRolesForGuild(IGuild guild) {
		return null;
	}

	@Override
	public Optional<String> getNicknameForGuild(IGuild guild) {
		return null;
	}

	@Override
	public boolean isBot() {
		return false;
	}

	@Override
	public void moveToVoiceChannel(IVoiceChannel newChannel) throws DiscordException, RateLimitException, MissingPermissionsException {

	}

	@Override
	public List<IVoiceChannel> getConnectedVoiceChannels() {
		return null;
	}

	@Override
	public IPrivateChannel getOrCreatePMChannel() throws RateLimitException, DiscordException {
		return null;
	}

	@Override
	public boolean isDeaf(IGuild guild) {
		return false;
	}

	@Override
	public boolean isMuted(IGuild guild) {
		return false;
	}

	@Override
	public boolean isDeafLocally() {
		return false;
	}

	@Override
	public boolean isMutedLocally() {
		return false;
	}

	@Override
	public void addRole(IRole role) throws MissingPermissionsException, RateLimitException, DiscordException {

	}

	@Override
	public void removeRole(IRole role) throws MissingPermissionsException, RateLimitException, DiscordException {

	}

	@Override
	public String getID() {
		return "12345";
	}

	@Override
	public IDiscordClient getClient() {
		return null;
	}

	@Override
	public IUser copy() {
		return null;
	}
}
