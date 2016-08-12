package novaz.main;

import novaz.core.ConfigurationBuilder;
import novaz.db.WebDb;
import sx.blah.discord.util.DiscordException;

import java.io.File;
import java.io.IOException;

public class Launcher {

	public static void main(String[] args) throws DiscordException, IOException {
		new ConfigurationBuilder(Config.class, new File("application.cfg")).build();
		WebDb.init();
		NovaBot nb = new NovaBot();
	}
}