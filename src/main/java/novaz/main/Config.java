package novaz.main;

import novaz.core.annotation.Option;

public class Config {
	//display name of the bot
	@Option
	public static String BOT_NAME = "NovaBot";
	//token used to login to discord
	@Option
	public static String BOT_TOKEN = "mybottokenhere";
	//prefix for all commands !help etc.
	@Option
	public static String BOT_COMMAND_PREFIX = "!";
	//location of youtubedl.exe
	@Option
	public static String YOUTUBEDL_EXE = "H:/youtube-dl.exe";
	//folder with the binary files required for youtubedl
	@Option
	public static String YOUTUBEDL_BIN = "H:/music/bin/";
	//directory where all the music is stored
	@Option
	public static String MUSIC_DIRECTORY = "H:/music/";
}
