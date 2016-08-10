package novaz.main;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

public class Launcher {

	static IDiscordClient client;

	public static void main(String[] args) throws DiscordException {
		NovaBot nb = new NovaBot();
	}
}
