# DiscordBot

A Java bot for [Discord](https://discordapp.com/) using the [Discord4J interface](https://github.com/austinv11/Discord4J/).  
It uses mysql to store data, a create script can be found in the sql folder  
To get music from youtube it makes use of [youtube-dl](https://github.com/rg3/youtube-dl) and [avconv](https://libav.org/avconv.html)



## What can it do?


* Respond to commands
* Add custom commands
* play/download music
* customizable globally and per guild/server

### Commands

Commands are prefixed with a "!" by default, this can be configured.
For a list of commands in discord the **help** command can be used.
For more information about a command use **help \<commandname\>**

Current list of all available commands. See below for a more detailed list

%_COMMANDS_LIST_SIMPLE_%

## Warning

This project is still in an early stage. Some things might not work or get changed drastically.

## Usage

On the first run it will generate a config file and stop running. You'll need to at least set the token and the property **bot_enabled** to true

## Command details

%_COMMANDS_LIST_DETAILS_%