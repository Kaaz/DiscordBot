package novaz.main;

import novaz.core.annotation.Option;

public class Config {
	//bot enabled? must be set to true in order to run
	@Option
	public static String BOT_ENABLED = "false";

	//display name of the bot
	@Option
	public static String BOT_NAME = "NovaBot";

	//token used to login to discord
	@Option
	public static String BOT_TOKEN = "mybottokenhere";

	//prefix for all commands !help etc.
	@Option
	public static String BOT_CHATTING_ENABLED = "true";

	//default prefix to mark messages as commands
	@Option
	public static String BOT_COMMAND_PREFIX = "!";

	//save the usage of commands?
	@Option
	public static String BOT_COMMAND_LOGGING = "true";

	//Reply to non existing commands?
	@Option
	public static String BOT_COMMAND_SHOW_UNKNOWN = "false";

	//location of youtubedl.exe
	@Option
	public static String YOUTUBEDL_EXE = "H:/youtube-dl.exe";

	//folder with the binary files required for youtubedl
	@Option
	public static String YOUTUBEDL_BIN = "H:/music/bin/";

	//directory where all the music is stored
	@Option
	public static String MUSIC_DIRECTORY = "H:/music/";

	//mysql hostname
	@Option
	public static String DB_HOST = "localhost";

	//mysql user
	@Option
	public static String DB_USER = "root";

	//mysql password
	@Option
	public static String DB_PASS = "";

	//mysql database name
	@Option
	public static String DB_NAME = "discord";

	//Use trello integration
	@Option
	public static String TRELLO_ACTIVE = "false";

	//Use trello integration
	@Option
	public static String TRELLO_API_KEY = "api-key-here";

	//Use trello integration
	@Option
	public static String TRELLO_OAUTH_SECRET = "secret-here";

	public static String EOL = System.getProperty("line.separator");
	public static long DELETE_MESSAGES_AFTER = 120000;
	public static boolean SHOW_KEYPHRASE = false;
	public static String CREATOR_ID = "97433066384928768";
}
