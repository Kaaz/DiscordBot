package novaz.main;

import novaz.core.annotation.Option;

public class Config {
	//display name of the bot

	@Option
	public static String BOT_ENABLED = "false";
	@Option
	public static String BOT_NAME = "NovaBot";
	//token used to login to discord
	@Option
	public static String BOT_TOKEN = "mybottokenhere";
	//prefix for all commands !help etc.
	@Option
	public static String BOT_CHATTING_ENABLED = "true";
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
	@Option
	public static String DB_HOST = "localhost";
	@Option
	public static String DB_USER = "root";
	@Option
	public static String DB_PASS = "";
	@Option
	public static String DB_NAME = "discord";

	public static String EOL = System.getProperty("line.separator");
	public static long DELETE_MESSAGES_AFTER = 120000;
	public static boolean SHOW_KEYPHRASE = false;
	public static String CREATOR_ID = "97433066384928768";
}
