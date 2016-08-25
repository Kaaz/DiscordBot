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

* [8ball](#8ball)
* [blackjack](#blackjack)
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
* [slot](#slot)
* [stop](#stop)
* [system](#system)
* [template](#template)
* [test](#test)
* [volume](#volume)


## Warning

This is mostly a toy/experiment project 

## Usage

On the first run it will generate a config file and stop running. You'll need to at least set the token and the property **bot_enabled** to true

## Command details

### 8ball

See what the magic 8ball has to say

### blackjack

Is being worked on!


#### Usage

```php
blackjack        //check status
blackjack hit    //hits
blackjack stand  //stands
```
### catfact

Cat facts!

### command

Add and remove custom commands


#### Usage

```php
command add <command> <action>  //adds a command
command delete <command>        //deletes a command
```
### config

Gets/sets the configuration of the bot


#### Usage

```php
config                    //overview
config <property>         //check details of property
config <property> <value> //sets property
```
### current

retrieves information about the song currently playing


#### Usage

```php
current               //info about the currently playing song
current title <title> //sets title of current song
current artist        //sets the artist of current song
current correct       //accept the systems suggestion of title/artist
current reversed      //accept the systems suggestion of title/artist in reverse [title=artist,artist=title]
```
### exit

completely shuts the bot down

### help

An attempt to help out


#### Usage

```php
help            //index of all commands
help <command>  //usage for that command
```
### info

Shows info about the bot

### invite

Provides an invite link to add the bot to your server.

### join

joins a voicechannel


#### Usage

```php
join                //attempts to join you
join <channelname>  //attempts to join channelname
```
### joke

An attempt to be funny

### leave

Leaves the voicechannel

### play

Plays a song from youtube


#### Usage

```php
play <youtubelink>        //download and plays song
play <youtubevideocode>   //download and plays song
play <part of youtubeTitle>      //shows search results
play #<resultnumber>      //add result # to the queue
```
### playlist

information about the playlist/history


#### Usage

```php
playlist          //playlist queue
playlist history  //list of recently played songs
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


#### Usage

```php
skip      //skips current track
skip perm //skips permanently; never hear this song again
```
### slot

Feeling lucky? try the slotmachine! You might just win a hand full of air!


#### Usage

```php
slot      //displays info and payout table
slot play //plays the game
```
### stop

stops playing music

### system

Shows memory usage

### template

adds/removes templates


#### Usage

```php
template list                        //lists all keyphrases
template list <keyphrase>            //lists all options for keyphrase
template remove <keyphrase> <index>  //removes selected template for keyphrase
template add <keyphrase> <text...>   //adds a template for keyphrase
template toggledebug                 //shows keyphrases instead of text
```
### test

no one truly knows


#### Usage

```php
lorem ipsum dolar sit amet
```
### volume

gets and sets the volume of the music


#### Usage

```php
volume              //shows current volume
volume <1 to 100>   //sets volume
```
