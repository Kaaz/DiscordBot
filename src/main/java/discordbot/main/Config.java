/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package discordbot.main;


import com.wezinkhof.configuration.ConfigurationOption;
import discordbot.util.Emojibet;

public class Config {

	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36";
	//the bot/users ratio for guilds
	public static final double GUILD_MAX_USER_BOT_RATIO = 0.75D;
	//the minimum age of the guild owner's account in days
	public static final long GUILD_OWNER_MIN_ACCOUNT_AGE = 7;
	//if a guild has less users it will be marked as a test guild
	public static final int GUILD_MIN_USERS = 5;
	//the default time to delete messages after milliseconds
	public static long DELETE_MESSAGES_AFTER = 120000;

	//bot enabled? must be set to true in order to run
	@ConfigurationOption
	public static boolean BOT_ENABLED = false;

	@ConfigurationOption
	public static boolean BOT_RESTART_INACTIVE_SHARDS = false;

	//token for discord.bots.pw
	@ConfigurationOption
	public static String BOT_TOKEN_BOTS_DISCORD_PW = "token-here";

	//toggle sending stats to discord.bots.pw
	@ConfigurationOption
	public static boolean BOT_STATS_DISCORD_PW_ENABLED = false;

	//the website of the bot
	@ConfigurationOption
	public static String BOT_WEBSITE = "emily-bot.pw";
	@ConfigurationOption
	public static String BOT_ENV = "test";

	//if you want to use graylog
	@ConfigurationOption
	public static boolean BOT_GRAYLOG_ACTIVE = false;
	@ConfigurationOption
	public static String BOT_GRAYLOG_HOST = "10.120.34.139";
	@ConfigurationOption
	public static int BOT_GRAYLOG_PORT = 12202;

	@ConfigurationOption
	public static boolean BOT_AUTO_UPDATE = false;

	@ConfigurationOption
	public static boolean BOT_ON_BOT_ACTION = false;
	//display name of the bot
	@ConfigurationOption
	public static String BOT_NAME = "Emily";

	//Bot's own discord server
	@ConfigurationOption
	public static String BOT_GUILD_ID = "225168913808228352";

	//Bot's own channel on its own server
	@ConfigurationOption
	public static String BOT_CHANNEL_ID = "225170823898464256";

	//Bot's error channel id
	@ConfigurationOption
	public static String BOT_ERROR_CHANNEL_ID = "249646038443491340";

	//Bot's status update
	@ConfigurationOption
	public static String BOT_STATUS_CHANNEL_ID = "260721966430814210";

	//token used to login to discord
	@ConfigurationOption
	public static String BOT_TOKEN = "mybottokenhere";

	//token used to login to discord by the restart bot, used when BOT_RESTART_INACTIVE_SHARDS is true
	@ConfigurationOption
	public static String BOT_RESTART_TOKEN = "mybottokenhere";

	//prefix for all commands !help etc.
	@ConfigurationOption
	public static boolean BOT_CHATTING_ENABLED = true;

	//default prefix to mark messages as commands
	@ConfigurationOption
	public static String BOT_COMMAND_PREFIX = "!";

	//save the usage of commands?
	@ConfigurationOption
	public static boolean BOT_COMMAND_LOGGING = true;

	//show keyphrases?
	@ConfigurationOption
	public static boolean SHOW_KEYPHRASE = false;

	//Reply to non existing commands?
	@ConfigurationOption
	public static boolean BOT_COMMAND_SHOW_UNKNOWN = false;

	//shows console output from the youtube-dl process
	@ConfigurationOption
	public static volatile boolean YOUTUBEDL_DEBUG_PROCESS = false;

	//location of youtubedl.exe
	@ConfigurationOption
	public static String YOUTUBEDL_EXE = "H:/youtube-dl.exe";

	//directory where all the music is stored
	@ConfigurationOption
	public static String MUSIC_DIRECTORY = "H:/music/";

	@ConfigurationOption
	public static int MUSIC_MAX_PLAYLIST_SIZE = 50;

	//save music on a different location while its being processed?
	@ConfigurationOption
	public static boolean MUSIC_USE_CACHE_DIR = false;

	//where the downloading music is placed till its processed
	@ConfigurationOption
	public static String MUSIC_CACHE_DIR = "/tmp/";


	//mysql hostname
	@ConfigurationOption
	public static String DB_HOST = "localhost";

	//mysql user
	@ConfigurationOption
	public static String DB_USER = "root";

	//mysql password
	@ConfigurationOption
	public static String DB_PASS = "";

	//mysql database name
	@ConfigurationOption
	public static String DB_NAME = "discord";

	//enable economy globally
	@ConfigurationOption
	public static boolean MODULE_ECONOMY_ENABLED = true;

	//enable poe globally
	@ConfigurationOption
	public static boolean MODULE_POE_ENABLED = true;

	//enable hearthstone globally
	@ConfigurationOption
	public static boolean MODULE_HEARTHSTONE_ENABLED = true;

	//enable music globally
	@ConfigurationOption
	public static boolean MODULE_MUSIC_ENABLED = true;

	//name of the currency
	@ConfigurationOption
	public static String ECONOMY_CURRENCY_NAME = "cookie";
	@ConfigurationOption
	public static String ECONOMY_CURRENCY_NAMES = "cookies";

	//emoticon of the currency
	@ConfigurationOption
	public static String ECONOMY_CURRENCY_ICON = Emojibet.COOKIE;

	//a new user starts with this balance
	@ConfigurationOption
	public static int ECONOMY_START_BALANCE = 1;
	//Use trello integration
	@ConfigurationOption
	public static boolean TRELLO_ACTIVE = false;

	@ConfigurationOption
	public static String[] GOOGLE_API_KEY = new String[]{"google-api-key-here"};

	@ConfigurationOption
	public static String GIPHY_TOKEN = "dc6zaTOxFJmzC";

	//Use trello integration
	@ConfigurationOption
	public static String TRELLO_API_KEY = "api-key-here";

	@ConfigurationOption
	public static String TRELLO_BOARD_ID = "57beb462bac8baf93c4bba47";

	@ConfigurationOption
	public static String TRELLO_LIST_BUGS = "57beb482265f090f6a425e01";

	@ConfigurationOption
	public static String TRELLO_LIST_IN_PROGRESS = "57beb4850d0e12837dca475d";

	@ConfigurationOption
	public static String TRELLO_LIST_PLANNED = "57beb4b9146625cc9f255073";

	//the trello token
	@ConfigurationOption
	public static String TRELLO_TOKEN = "token-here";

	public static String EOL = System.getProperty("line.separator");
	@ConfigurationOption
	public static String CREATOR_ID = "97433066384928768";
}
