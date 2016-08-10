package novaz.main;

import novaz.db.WebDb;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

public class Launcher {

	static IDiscordClient client;

	public static void main(String[] args) throws DiscordException {
		WebDb.init();
		NovaBot nb = new NovaBot();
	}
}
