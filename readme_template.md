# Emily - A discord bot

A Java bot for [Discord](https://discordapp.com/) using the [JDA library](https://github.com/DV8FromTheWorld/JDA).

It uses sql (mariadb) to store data  
If you'd like to run your own version of this project check the [installation part](#run-the-bot-yourself)

You can play/test it on discord 
[![Discord](https://discordapp.com/api/guilds/365760529899192322/widget.png)](https://discord.gg/7aKuSj5)
## What can it do?


* Ton of commands
* Add your own
* play music
* [customizable globally](#global-configuration) and [per guild](#per-guild-configuration)
* play games against other users
* subscription system
* various optional modules
* auto self updates with [this project](https://github.com/Kaaz/discord-bot-manager)
* [auto ranking system](#ranking-system)

### Commands

Commands are prefixed with a "!" by default, this can be configured.
For a list of commands in discord the **help** command can be used.
For more information about a command use **help \<commandname\>**

Current list of all available commands. See below for a more detailed list

%_COMMANDS_LIST_SIMPLE_%

## Games

Games can be accessed though the **!game** command

A list of games:

%_LIST_OF_GAMES_%

## Per guild configuration

The per-guild configuration can be accessed though the **!config** command, to use it you must be the server owner, or have the discord-administrator permission
 
The following settings can be changed per guild:

%_CONFIG_PER_GUILD_%


## Ranking system
The auto ranking system is based on the join date of the user. After a set amount of time the user will be promoted to the next rank.
The ranks will be created/maintained by the bot.

The following settings will affect the ranking system
[user_time_ranks](#per-guild-configuration)
[user_time_ranks_prefix](#per-guild-configuration)

*note: In order for this to work the bot needs the manage roles permission on discord.*

The rankings go according to the table below:

%_LIST_OF_AUTO_RANKS_%

## Run the bot yourself

* [Java 8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [git](https://git-scm.com/)
* [gradle](https://gradle.org/)

Also prepare a [MariaDB SQL](https://mariadb.org/) server (either hosted or installed locally).<br/>
Then continue with the following steps.

1. Create a database

    Create a database using the utf8mb4 character set. <br/>
    To make this easier you can do this using a SQL management tool like [PHPMyAdmin](https://www.phpmyadmin.net/) or [HeidiSQL](http://www.heidisql.com/).

2. Clone the project with git

    ```
    cd /path/to/your/project/folder
    git clone https://github.com/Kaaz/DiscordBot.git
    ```  

3. Collect dependencies
    
    `cd` into the directory that git just created (should be called `DiscordBot`).<br>
    Then type `gradle install`.
    
4. Build
    
    Type `mvn fatJar` in the `DiscordBot` folder.
    In the build/libs/ folder there should be a file called `Emily-all-{VERSION}.jar` (where version is the latest version number).
    Move this file over to a location wherever you want to start the bot from.
    
5. Run

    You can launch the bot with the following command:
    `java -jar <jarfilename>`
    On first launch, It will generate an `application.cfg` file and exit.<br>
    You'll have to edit the config file and add in your token, database configuration, etc.


## Usage

On the first run it will generate a config file and stop running. You'll need to at least set the token and the property **bot_enabled** to true

## Command details

%_COMMANDS_LIST_DETAILS_%


## Global configuration

The global configuration is stored in the application.cfg file, which is generated the first time you run the application

The following settings can be set globally:

Setting name | default | description
---|---|---
BOT_ENABLED | false | Enables the bot<br/> This must be set to true in order to run the bot
BOT_AUTO_UPDATE | false | Enable automatic updates. <br/>For this to work you'll have to launch the bot though my other project<br/>https://github.com/Kaaz/ConfigurationBuilder
BOT_NAME | "NovaBot" | the default name of the bot,
BOT_CHANNEL_ID | "225170823898464256" | Discord channel is of the bot's own channel
BOT_TOKEN | "mybottokenhere" | token used to login to discord
BOT_CHATTING_ENABLED | true | Enable cleverbot
BOT_COMMAND_PREFIX | "!" |  prefix for all commands !help etc. This can be overriden per guild
BOT_COMMAND_LOGGING | true | save the usage of commands
BOT_COMMAND_SHOW_UNKNOWN | false | Reply to non existing commands? <br/> eg. hey that command doesn't exist
MUSIC_DOWNLOAD_SOUNDCLOUD_EXE | "H:/" | location of the soundcloud jar
MUSIC_DOWNLOAD_SOUNDCLOUD_API_TOKEN | "some-token" | token used to connect to soundcloud
YOUTUBEDL_EXE | "H:/youtube-dl.exe" | location of youtubedl.exe
YOUTUBEDL_BIN | "H:/music/bin/" | folder with the binary files required for ffmpeg
MUSIC_DIRECTORY | "H:/music/" | directory where all the music is stored
DB_HOST | "localhost" | sql hostname
DB_USER | "root" | sql user
DB_PASS | "" | sql password
DB_NAME | "discord" | sql database name
MODULE_ECONOMY_ENABLED | true | enable economy globally
MODULE_POE_ENABLED | true | enable poe globally
MODULE_HEARTHSTONE_ENABLED | true | enable hearthstone globally
MODULE_MUSIC_ENABLED | true | enable music globally
ECONOMY_CURRENCY_NAME | "" | name of the currency
ECONOMY_CURRENCY_ICON | "" | emoticon of the currency
TRELLO_ACTIVE | false | Use trello integration
TRELLO_API_KEY | "api-key-here" | Use trello integration
TRELLO_BOARD_ID | "1234" | trello board id
TRELLO_LIST_BUGS | "1234" | trello list id
TRELLO_LIST_IN_PROGRESS | "1234" |  trello list id for in progress items
TRELLO_LIST_PLANNED | "1234" | trello list id for planned items
TRELLO_TOKEN | "token-here" | the trello token


