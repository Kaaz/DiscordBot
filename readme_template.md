# DiscordBot

A Java bot for [Discord](https://discordapp.com/) using the [Discord4J interface](https://github.com/austinv11/Discord4J/).  
It uses mysql to store data, a create script can be found in the sql folder  
To get music from youtube it makes use of [youtube-dl](https://github.com/rg3/youtube-dl) and [avconv](https://libav.org/avconv.html)

You can play/test it on discord 
[![Discord](https://discordapp.com/api/guilds/225168913808228352/widget.png)](https://discord.gg/eaywDDt)
## What can it do?


* Respond to commands
* Add custom commands
* play/download music
* customizable globally and per guild/server
* play games against other users
* subscription system
* various optional modules
* [auto ranking system](#Ranking system)

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

The configuration can be accessed though the **!config** command
 
The following settings can be changed per guild:

%_CONFIG_PER_GUILD_%


## Ranking system
The auto ranking system is based on the join date of the user. After a set amount of time the user will be promoted to the next rank.
The ranks will be created/maintained by the bot.

The following settings will affect the ranking system
[user_time_ranks](#Per guild configuration)
[user_time_ranks_prefix](#Per guild configuration)

*note: In order for this to work the bot needs the manage roles permission on discord.*

The rankings go according to the table below:

%_LIST_OF_AUTO_RANKS_%


## Warning

This project is still in an early stage. Some things might not work or get changed drastically.

## Usage

On the first run it will generate a config file and stop running. You'll need to at least set the token and the property **bot_enabled** to true

## Command details

%_COMMANDS_LIST_DETAILS_%