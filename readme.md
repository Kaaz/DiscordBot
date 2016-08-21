# DiscordBot

A Java bot for [Discord](https://discordapp.com/) using the [Discord4J interface](https://github.com/austinv11/Discord4J/).


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

* [8ball](#8ball)
* [catfact](#catfact)
* [command](#command)
* [config](#config)
* [current](#current)
* [exit](#exit)
* [help](#help)
* [info](#info)
* [invite](#invite)
* [join](#join)
* [joke](#joke)
* [leave](#leave)
* [play](#play)
* [playlist](#playlist)
* [reload](#reload)
* [say](#say)
* [skip](#skip)
* [stop](#stop)
* [template](#template)
* [volume](#volume)


## Warning

This is mostly a toy/experiment project 

## Usage

On the first run it will generate a config file and stop running. You'll need to at least set the token and the property **bot_enabled** to true

## Command details

### 8ball

See what the magic 8ball has to say

### catfact

Cat facts!

### command

Add and remove custom commands


#### Usage

```php
command add <command> <action> //adds a command
command delete <command> //deletes a command
```
### config

Gets/sets the configuration of the bot


#### Usage

```php
config //overview
config <property> //check details of property
config <property> <value> //sets property
```
### current

retrieves information about the song currently playing

### exit

completely shuts the bot down

### help

An attempt to help out


#### Usage

```php
help //index of all commands
help <command> //usage for that command
```
### info

Shows info about the bot

### invite

Provides an invite link to add the bot to your server.

### join

joins a voicechannel


#### Usage

```php
join //attempts to join you
join <channelname> //attempts to join channelname
```
### joke

An attempt to be funny

### leave

Leaves the voicechannel

### play

Plays a song from youtube


#### Usage

```php
play <youtubelink>
play <youtubevideocode>
```
### playlist

information about the playlist/history


#### Usage

```php
playlist //playlist queue
playlist history //list of recently played songs
```
### reload

reloads the configuration

### say

repeats you


#### Usage

```php
say <anything>
```
### skip

skip current track

### stop

stops playing music

### template

adds/removes templates


#### Usage

```php
template list //lists all keyphrases
template list <keyphrase> //lists all options for keyphrase
template remove <keyphrase> <index> //removes selected template for keyphrase
template add <keyphrase> <text...> //adds a template for keyphrase
template toggledebug //shows keyphrases instead of text
```
### volume

gets and sets the volume of the music


#### Usage

```php
volume //shows current volume
volume <1 to 100> //sets volume
```
