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

Commands | | | | |
--- | --- | ---| ---| ---
[8ball](#8ball) | [ban](#ban) | [blackjack](#blackjack) | [botstatus](#botstatus) | [case](#case)
[catfact](#catfact) | [changename](#changename) | [cla](#cla) | [command](#command) | [commandadmin](#commandadmin)
[config](#config) | [current](#current) | [debug](#debug) | [donate](#donate) | [exec](#exec)
[exit](#exit) | [fight](#fight) | [fml](#fml) | [game](#game) | [getrole](#getrole)
[gif](#gif) | [globalban](#globalban) | [guildstats](#guildstats) | [help](#help) | [info](#info)
[invite](#invite) | [join](#join) | [joke](#joke) | [kick](#kick) | [leaveguild](#leaveguild)
[logging](#logging) | [meme](#meme) | [modcase](#modcase) | [music](#music) | [mute](#mute)
[pause](#pause) | [ping](#ping) | [play](#play) | [playlist](#playlist) | [pm](#pm)
[poll](#poll) | [prefix](#prefix) | [purge](#purge) | [queue](#queue) | [reactionrole](#reactionrole)
[reboot](#reboot) | [reddit](#reddit) | [reload](#reload) | [report](#report) | [roleadmin](#roleadmin)
[roll](#roll) | [rotate](#rotate) | [sendfile](#sendfile) | [server](#server) | [skip](#skip)
[slot](#slot) | [stop](#stop) | [subscribe](#subscribe) | [system](#system) | [tag](#tag)
[tempban](#tempban) | [template](#template) | [test](#test) | [todo](#todo) | [ud](#ud)
[uptime](#uptime) | [user](#user) | [userrank](#userrank) | [version](#version) | [volume](#volume)
[warn](#warn) | 

## Games

Games can be accessed though the **!game** command

A list of games:

Key | Name | Players |
--- | --- | --- |
cf | Connect Four | 2
gos | Game of sticks | 2
tic | Tic tac toe | 2


## Per guild configuration

The per-guild configuration can be accessed though the **!config** command, to use it you must be the server owner, or have the discord-administrator permission
 
The following settings can be changed per guild:


### AUTO_REPLY
default: `false`  
setting-type: `toggle`

use the auto reply feature?  
Looks for patterns in messages and replies to them (with a cooldown)  
true -> enable auto replying to matched messages  
false -> disable auto replying
### BOT_ADMIN_ROLE
default:   
setting-type: `discord-role`

Users with this role are considered admins for the bot
### BOT_CHANNEL
default: `general`  
setting-type: `text-channel`

Channel where the bots default output goes to
### BOT_LANGUAGE
default: `en`  
setting-type: `enum [de, en, nl]`

The output language of the bot
### BOT_LOGGING_CHANNEL
default: `false`  
setting-type: `text-channel`

The channel where the logging of events happens. Such as users joining/leaving  
  
Setting this to 'false' will disable it (without the quotes)  
  
To enable it, set this setting to match the channel name where you want the logging to happen  
If you specify an invalid channel, this setting will disable itself
### BOT_MODLOG_CHANNEL
default: `false`  
setting-type: `text-channel`

The channel where mod-logging happens.  
A case will appear if a user has been banned/kicked/warned/muted  
  
Setting this to 'false' will disable it (without the quotes)  
  
To enable it, set this setting to match the channel name where you want the moderation-cases to go  
If you specify an invalid channel, this setting will disable itself
### BOT_MUTE_ROLE
default: `false`  
setting-type: `discord-role`

This is the role which is applied to those who you use the mute command on  
  
Setting this value to false will disable the role applied with the mute command
### BOT_UPDATE_WARNING
default: `playing`  
setting-type: `enum [always, playing, off]`

Show a warning that there is an update and that the bot will be updating soon.  
always  -> always show the message in the bot's configured default channel  
playing -> only announce when the bot is playing music and in the bot's configured music channel  
off     -> don't announce when the bot is going down for an update
### CHAT_BOT_ENABLED
default: `false`  
setting-type: `toggle`

Setting this to true will make it so that it responds to every message in the configured bot_channel
### CLEANUP_MESSAGES
default: `no`  
setting-type: `enum [no, yes, nonstandard]`

Delete messages after a while?  
yes         -> Always delete messages  
no          -> Never delete messages  
nonstandard -> delete messages outside of bot's default channel
### COMMAND_LOGGING_CHANNEL
default: `false`  
setting-type: `text-channel`

The channel command usage will be logged to  
  
Example output:  
Kaaz#9436 has used `say` in #general  
arguments: this is not a test  
output: this is not a test  
  
Setting this to 'false' will disable it (without the quotes)  
To enable it, set this setting to match the channel name where you want the command logging to happen  
If you specify an invalid channel, this setting will disable itself
### COMMAND_PREFIX
default: `$`  
setting-type: `enum`

Prefix for commands (between 1 and 4 characters)
### DEBUG
default: `false`  
setting-type: `toggle`

Show some debug information.  
  
valid values:   
true       -> Show a lot more additional information  
 false      -> don't   
  
If you want to check if certain things are (not) working.  
  

### HELP_IN_PM
default: `false`  
setting-type: `toggle`

show help in a private message?  
true  -> send a message to the user requesting help  
false -> output help to the channel where requested
### MODULE_ECONOMY
default: `true`  
setting-type: `toggle`

Use the economy feature?  
false -> nope!  
true -> yep!
### MODULE_GAMES
default: `true`  
setting-type: `toggle`

Let people play games against each other
### MUSIC_CHANNEL
default: `false`  
setting-type: `text-channel`

Channel where the bots music-related output goes to
### MUSIC_CHANNEL_AUTO
default: `false`  
setting-type: `voice-channel`

The channel where I automatically connect to if a user joins  
  
false:  
Not using this setting, wont auto-connect to anything.  
  
setting this to match a voice channel name:  
The moment a user connects to the specified channel I connect too and start to play music.  
  
Important to note:   
* If the configured channel does not exist, this setting will be turned off  
* If I'm already connected to a different voice-channel I won't use this setting  

### MUSIC_CHANNEL_TITLE
default: `false`  
setting-type: `enum [auto, true, false]`

Updates the music channel's topic with the currently playing song  
  
auto  -> update the title every 10 seconds with the track its playing  
true  -> yes change the topic at the beginning of every song  
false -> leave the channel topic title alone!
### MUSIC_CLEAR_ADMIN_ONLY
default: `true`  
setting-type: `toggle`

Only allow admins to clear the music add?  
  
true  
Only admins can clear the music add  
  
false  
Everyone can clear the add
### MUSIC_PLAYING_MESSAGE
default: `clear`  
setting-type: `enum [normal, clear, off]`

Clear the now playing message?  
clear  -> sends a message and deletes it when the song is over or skipped  
normal -> send the message and just leave it be  
off    -> don't send now playing messages
### MUSIC_QUEUE_ONLY
default: `false`  
setting-type: `toggle`

Stop playing music once the add is empty?  
  
true  
once the add is empty I stop playing music and leave the voice channel  
  
false  
If the add is empty, I'm gonna pick the track.
### MUSIC_RESULT_PICKER
default: `1`  
setting-type: `enum`

the amount of results the `play` command returns  
  
If its set to 1, it will always use the first result (no manual choice)  
  
If its set higher (max 5) it will respond with reactions where each button is a choice  
Note: This setting does require the add reactions permission
### MUSIC_ROLE_REQUIREMENT
default: `false`  
setting-type: `discord-role`

In order to use music commands you need this role!  
Setting this value to false will disable the requirement
### MUSIC_SHOW_LISTENERS
default: `false`  
setting-type: `toggle`

Show who's listening in the *current* command  
true  -> List all the people who are currently listening to music  
false -> Don't show listeners
### MUSIC_SKIP_ADMIN_ONLY
default: `false`  
setting-type: `toggle`

Only allow admins to use the skip command?  
  
true  
Only admins have permission to use the skip command  
  
false  
Everyone can use the skip command
### MUSIC_VOLUME
default: `100`  
setting-type: `enum`

sets the default volume of the music player  
So the next time the bot connects it starts with this volume  
  
Accepts a value between 0 and 100
### MUSIC_VOLUME_ADMIN
default: `false`  
setting-type: `toggle`

Require a guild admin to change the volume  
  
true -> only allow guild admins to change the bot's volume  
false -> allow all users to change the bot's volume
### MUSIC_VOTE_PERCENT
default: `40`  
setting-type: `enum`

Percentage of users (rounded down) required to skip the currently playing track  
  
eg; when set to 25, and 5 listeners it would require 2 users to vote skip  
  
Accepts a value between 1 and 100
### PM_USER_EVENTS
default: `false`  
setting-type: `toggle`

Send a private message to owner when something happens to a user?  
true  -> sends a private message to guild-owner  
false -> does absolutely nothing
### SHOW_TEMPLATES
default: `false`  
setting-type: `toggle`

Show which templates are being used on places.  
  
valid values:   
true       -> Shows the keyphrases being used  
 false      -> Shows normal text   
  
for instance if you don't have permission to access a command:  
  
setting this to true would show:  
no_permission  
  
false would show:  
You don't have permission to use that!
### SHOW_UNKNOWN_COMMANDS
default: `false`  
setting-type: `toggle`

Show message on nonexistent commands and blacklisted commands  
true -> returns a help message  
false -> stays silent
### USER_TIME_RANKS
default: `false`  
setting-type: `toggle`

This setting will require me to have the manage role permission!  
Users are given a role based on their time spend in the discord server  
If you'd like to use the time based ranks, be sure to check out the other settings first!  
Setting:  Use time based ranks?  
true  -> yes  
false -> no
### USER_TIME_RANKS_PREFIX
default: `[rank]`  
setting-type: `enum`

The prefix of the role name for the time based role ranking  
Using this prefix to manage roles so make sure its somewhat unique! Or you'll have to cleanup yourself :)  
If you'd like to use the time based ranks make sure to set this first!  
  
The prefix can be between 3 and 8 in length
### WELCOME_NEW_USERS
default: `false`  
setting-type: `toggle`

Show a welcome message to new users?  
Valid options:  
true  -> shows a welcome when a user joins or leaves the guild  
false -> Disabled, doesn't say anything  
  
The welcome message can be set with the template:   
welcome_new_user  
  
The welcome back message can be set with the template (if the user had joined before):   
welcome_back_user  
  
The leave message can be set with the template:   
message_user_leaves  
  
If multiple templates are set a random one will be chosen  
See the template command for more details


## Ranking system
The auto ranking system is based on the join date of the user. After a set amount of time the user will be promoted to the next rank.
The ranks will be created/maintained by the bot.

The following settings will affect the ranking system
[user_time_ranks](#per-guild-configuration)
[user_time_ranks_prefix](#per-guild-configuration)

*note: In order for this to work the bot needs the manage roles permission on discord.*

The rankings go according to the table below:

Name | Time spend |
--- | --- | 
Spectator | 16 minutes 
Outsider | about an hour 
Lurker | 4 hours 
Neutral | about a day
Prospect | 2 days 
Friendly | 4 days 
Regular | 7 days 
Honored | 14 days 
Veteran | 28 days 
Revered | 50 days 
Herald | 75 days 
Exalted | 125 days 
Beloved | 200 days 
Favorite | 365 days 
Consul | 500 days 


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
    
    Type `gradle fatJar` in the `DiscordBot` folder.
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

### 8ball

See what the magic 8ball has to say

Aliases: 8ball

Usable in public and private channels
### ban

bans a member from your guild

Aliases: ban

Usable in public  channels

#### Usage

```php
ban <user>     //Permanently removes user from guild user from guild
```
### blackjack

play a game of blackjack!

Aliases: blackjack, bj

Usable in public and private channels

#### Usage

```php
blackjack        //check status
blackjack hit    //hits
blackjack stand  //stands
```
### botstatus

Set the game I'm currently playing

Aliases: botstatus

Usable in public and private channels

#### Usage

```php
botstatus reset                      //unlocks the status
botstatus game <game>                //changes the playing game to <game>
botstatus stream <username> <game>   //streaming twitch.tv/<username> playing <game>
```
### case

Moderate the mod-cases

Aliases: case

Usable in public  channels

#### Usage

```php
case reason <id> <message>  //sets/modifies the reason of a case
case reason last <message> //sets/modified the reason of the last added case by you
```
### catfact

Cat facts!

Aliases: catfact, catfacts

Usable in public and private channels
### changename

Changes my name

Aliases: changename

Usable in public and private channels
### cla

manage the changelog

Aliases: cla

Usable in public and private channels

#### Usage

```php
cla <version> <type> <message>     //adds a change to <version> of <type> with <message>
cla current <type> <message>       //shortcut for current version
cla next <type> <message>          // ^ next version
cla types
cla <version> publish <true/false> //publish the log for version (or not)
```
### command

Add and remove custom commands.
There are a few keywords you can use in commands. These tags will be replaced by its value 

Key                Replacement
---                ---
%user%             Username 
%args%             everything the user said besides the command 
%arg1%             the first argument of the user 
%arg9%             the 9th argument etc. a new argument starts after a space 
%user-mention%     Mentions user 
%user-id%          ID of user
%nick%             Nickname
%discrim%          discrim
%guild%            Guild name
%guild-id%         guild id
%guild-users%      amount of users in the guild
%channel%          channel name
%channel-id%       channel id
%channel-mention%  Mentions channel
%rand-user%        random user in guild
%rand-user-online% random ONLINE user in guild

Aliases: command, cmd, customcommand

Usable in public  channels

#### Usage

```php
command add <command> <action>  //adds a command
command delete <command>        //deletes a command
command                         //shows a list of existing custom commands
```
### commandadmin

Commands can be enabled/disabled through this command.
A channel specific setting will always override the guild setting

You can also give/deny permission to roles to use certain commands

Aliases: commandadmin, ca

Usable in public  channels

#### Usage

```php
ca <command> [enable/disable]               //enables/disables commands in the whole guild
ca <command> [enable/disable] [#channel]    //enables/disables commands in a channel. This overrides the above
ca all-commands [enable/disable]            //disable/enable all (disable-able commands)
ca all-commands [enable/disable] [#channel] //disable/enable all commands in that channel

ca resetchannel [#channel]                  //resets the overrides for a channel
ca resetallchannels                         //resets the overrides for all channels
ca reset yesimsure                          //enables all commands + resets overrides

examples:
ca meme disable                             //this disabled the meme command
ca meme enable #spam                        //overrides and meme is enabled in #spam
```
### config

Gets/sets the configuration of the bot

Aliases: config, setting, cfg

Usable in public  channels

#### Usage

```php
config                    //overview
config page <number>      //show page <number>
config tags               //see what tags exist
config tag <tagname>      //show settings with tagname
config <property>         //check details of property
config <property> <value> //sets property

config reset yesimsure    //resets the configuration to the default settings
```
### current

retrieves information about the song currently playing

Aliases: current, playing, np, nowplaying

Usable in public  channels

#### Usage

```php
current                 //info about the currently playing song
current seek <time>     //go to specified timestamp of track (eg. 3m10s)
current vote <1-10>     //Cast your vote to the song; 1=worst, 10=best
current repeat          //repeats the currently playing song
current update          //updates the now playing message every 10 seconds
current updatetitle     //updates the topic of the music channel every 10 seconds
current source          //Shows the source of the video
current pm              //sends you a private message with the details

current clear               //clears everything in the queue
current clear admin         //check if clear is admin-only
current clear admin toggle  //switch between admin-only and normal
```
### debug

some debugging tools

Aliases: debug

Usable in public and private channels

#### Usage

```php
fixusernames, fixrelations 
```
### donate

general info about how to contribute or donate to Emily

Aliases: donate, contribute

Usable in public and private channels
### exec

executes commandline stuff

Aliases: exec

Usable in public and private channels
### exit

completely shuts the bot down

Aliases: exit, brexit

Usable in public and private channels
### fight

get in an epic fight; (gif fight)

Aliases: fight

Usable in public  channels

#### Usage

```php
fight         //random user fights
fight <user>  //<user> fights
```
### fml

fmylife! Returns a random entry from fmylife.com

Aliases: fml

Usable in public and private channels
### game

play games against eachother!

Aliases: game

Usable in public  channels

#### Usage

```php
game list                 //to see a list games
game <@user> <gamecode>   //play a game against @user
game cancel               //cancel an active game!
```
### getrole

allows users to request a role

Aliases: getrole

Usable in public  channels

#### Usage

```php
list                //see what roles are available
remove <rolename>   //removes the <rolename> from you
<rolename>          //assign the <rolename> to you 
```
### gif

Gifs from giphy

Aliases: gif

Usable in public and private channels

#### Usage

```php
gif         //shows random gif
gif <tags>  //random gif based on tags
```
### globalban

Ban those nasty humans

Aliases: globalban

Usable in public and private channels
### guildstats

shows some statistics

Aliases: guildstats, stats

Usable in public and private channels

#### Usage

```php
stats         //stats!
stats mini    //minified!
stats users   //graph of when users joined!
stats activity//last activity per shard
```
### help

An attempt to help out

Aliases: help, ?, halp, helpme, h, commands

Usable in public and private channels

#### Usage

```php
help            //shows commands grouped by categories, navigable by reactions 
help full       //index of all commands, in case you don't have reactions
help <command>  //usage for that command
```
### info

Shows some general information about me and my future plans.

Aliases: info, about

Usable in public and private channels

#### Usage

```php
info          //general info
```
### invite

Provides an invite link to add the bot to your server.

Aliases: invite, inv

Usable in public and private channels
### join

joins a voicechannel

Aliases: join

Usable in public  channels

#### Usage

```php
join                //attempts to join you
join <channelname>  //attempts to join channelname
```
### joke

An attempt to be funny

Aliases: joke

Usable in public and private channels
### kick

Kicks a member from your guild

Aliases: kick

Usable in public  channels

#### Usage

```php
kick <user>     //Remove user from the guild user from guild
```
### leaveguild

leaves guild :(

Aliases: leaveguild

Usable in public and private channels

#### Usage

```php
leaveguild     //leaves the guild
```
### logging

log all the things! Configure how/where/what is being logged

Aliases: logging, log

Usable in public and private channels
### meme

generate a meme!

Aliases: meme

Usable in public and private channels

#### Usage

```php
meme type                             //list of all valid types
meme <type> <toptext> | <bottomtext>  //make the meme!
meme <type> <toptext>                 //with just toptext!

example: 
meme sohappy If I could use this meme | I would be so happy
```
### modcase

Modcases

Aliases: modcase, case

Usable in public  channels

#### Usage

```php
kick <user>            //kicks user
```
### music

gets and sets the music-related settings

Aliases: music

Usable in public  channels

#### Usage

```php
music                   //shows music configuration
```
### mute

Mute a member from your guild

Aliases: mute

Usable in public  channels

#### Usage

```php
mute <user>     //Adds the configured muted role to user user from guild
```
### pause

pauses the music or resumes it if its paused

Aliases: pause, resume

Usable in public  channels
### ping

checks the latency of the bot

Aliases: ping

Usable in public and private channels

#### Usage

```php
ping                         //Check bot latency
ping fancy                   //Check bot latency in a fancier way
```
### play

Plays a song from youtube

Aliases: play, p

Usable in public  channels

#### Usage

```php
play <youtubelink>    //download and plays song
play <part of title>  //shows search results
play                  //just start playing something
```
### playlist

information about the playlists

Aliases: playlist, pl

Usable in public  channels

#### Usage

```php
-- using playlists 
playlist mine          //use your default playlist
playlist mine <code>   //use your playlist with code
playlist lists         //see what playlists you have 
playlist guildlists    //see what playlists the guild has
playlist guild         //use the guild's default playlist
playlist guild <code>  //use the guild's playlist with code
playlist global        //use the global playlist
playlist settings      //check the settings for the active playlist
playlist               //info about the current playlist
playlist list <page>   //Shows the music in the playlist

-- Adding and removing music from the playlist
playlist add                         //adds the currently playing music
playlist add guild                   //adds the currently playing to the guild list
playlist remove                      //removes the currently playing music
playlist removeall                   //removes ALL songs from playlist

-- Changing the settings of the playlist
playlist title <new title>           //edit the playlist title
playlist edit <new type>             //change the edit-type of a playlist
playlist play <id>                   //plays a track from the playlist
playlist playtype <new type>         //change the play-type of a playlist
```
### pm

Send a message to user

Aliases: pm

Usable in public and private channels

#### Usage

```php
pm <@user> <message..>
```
### poll

Strawpoll: propose a question and choices for the chat to vote on

Aliases: poll

Usable in public  channels

#### Usage

```php
poll          //status of active poll 
poll create <question> ;<option1>;<option2>;<etc.>   (max 8)
              //creates a poll for the duration
poll 1-9      //vote on the options
```
### prefix

Forgot what the prefix is? I got you covered

Aliases: prefix

Usable in public and private channels

#### Usage

```php
prefix                           //shows the set prefix
prefix <prefix>                  //sets the prefix to <prefix>
```
### purge

deletes non-pinned messages

Aliases: purge, clear, delete

Usable in public  channels

#### Usage

```php
//deletes up to 100 non-pinned messages
purge
//deletes <limit> (max 2500) non-pinned messages
purge <limit>
//deletes messages newer than now - (input)
purge time 1d2h10m         //you can use dhms and combinations 
//deletes <limit> messages from <user>, limit is optional
purge @user [limit]
//deletes messages from <user>, user can be part of a user's name
purge user <user>
//deletes messages matching <regex>
purge matches <regex>
//delete messages NOT matching <regex>
purge notmatches <regex>
//delete command related messages
purge commands
//deletes bot messages
purge bot
```
### queue

check whats in the music queue

Aliases: queue, q

Usable in public  channels

#### Usage

```php
queue        //overview
```
### reactionrole

Adds and removes roles from users based on reactions from a message

You save messages/reactions to keys to make maintaining them a little easier.

Aliases: reactionrole, rr

Usable in public  channels

#### Usage

```php
rr //overview of all the configured keys
rr add <key> <emote> <role> //adds a reaction with role to the message
rr remove <key> <emote>     //removes emote reaction from key
rr delete <key>             //deletes the set
rr message <key> <message>  //updates the message
rr display <key> [channel]  //displays the message in this channel
                            //or in the channel you specified
```
### reboot

restarts the bot

Aliases: reboot, restart

Usable in public and private channels

#### Usage

```php
reboot now              //reboots the system
reboot now firm         //reboots the system, but ensures a restart in 5 minutes
reboot update           //reboots the system and updates
reboot update firm      //reboots the system and updates, but ensures a restart in 5 minutes
reboot shard <id>       //reboots shard
reboot shard <guildid>  //reboots shard for guild-id
```
### reddit

Posts something from reddit

Aliases: reddit, r

Usable in public and private channels

#### Usage

```php
r <subreddit>
```
### reload

reloads the configuration

Aliases: reload

Usable in public  channels
### report

Report bugs/abuse/incidents

Aliases: report

Usable in private channels

#### Usage

```php
report <subject> | <message..>
```
### roleadmin

Management of roles & general permissions 
You can give users the ability to self-assign roles. 

Note: 
self-assignable roles are not created by emily!
To add an assignable role, you'll first have to add that role though discord.


Users can get/remove their own roles with the `getrole` command 

Aliases: roleadmin, ra

Usable in public  channels

#### Usage

```php
You can specify which roles are self-assignable by users with the following commands: 

roleadmin self                                 //check what roles are self-assignable
roleadmin self add <rolename>                  //add a role to the list of assignable roles
roleadmin self remove <rolename>               //remove a role from the list of assignable roles

//You can use everyone as <user> and it'll apply to everyone
roleadmin give <user> <role>     //gives a user a role
roleadmin take <user> <role>     //takes away role

roleadmin                        //lists roles
roleadmin cleanup                //cleans up the roles from the time-based rankings
roleadmin setup                  //creates the roles for the time-based rankings
```
### roll

if you ever need a random number

Aliases: roll, dice, rng

Usable in public and private channels

#### Usage

```php
roll               //random number 1-6
roll <max>         //random number 1-<max>
roll <min> <max>   //random number <min>-<max>
roll XdY           //eg. 2d5 rolls 2 dice of 1-5 and returns the sum
roll XdY+z         //eg. 2d5+2 rolls 2 dice of 1-5 and returns the sum plus 2
```
### rotate

Rotate text!

Aliases: rotate

Usable in public and private channels

#### Usage

```php
rotate <text..> 
```
### sendfile

executes commandline stuff

Aliases: sendfile

Usable in public and private channels
### server

Information about the server

Aliases: server

Usable in public  channels
### skip

skip current track

Aliases: skip, next

Usable in public  channels

#### Usage

```php
skip                  //skips current track
skip adminonly        //check what skipmode its set on
skip adminonly toggle //toggle the skipmode
skip force            //admin-only, force a skip
```
### slot

Feeling lucky? try the slotmachine! You might just win a hand full of air!

Aliases: slot

Usable in public and private channels

#### Usage

```php
slot              //spin the slotmachine
slot [cookies]    //play for real cookies where [cookies] is the amount of cookies you bet
slot info         //info about payout
```
### stop

stops playing music

Aliases: stop, leave

Usable in public  channels

#### Usage

```php
stop          //stops playing and leaves the channel
stop force    //stops playing and leaves the channel (admin, debug)
stop afternp  //stops and leaves after the now playing track is over
```
### subscribe

subscribe the channel to certain events

Aliases: subscribe, sub

Usable in public  channels

#### Usage

```php
subscribe                //check what subscriptions are active
subscribe <name>         //subscribe to subject
subscribe stop <name>    //stop subscription to subject
subscribe list           //See what subscription options there are
```
### system

Shows memory usage

Aliases: system, sysinfo, sys

Usable in public and private channels
### tag

Tags!

Aliases: tag, t, tags

Usable in public  channels

#### Usage

```php
tag                     //list of tags
tag <name>              //shows the tag
tag mine                //shows your tags
tag by <name>           //shows tags created by user
tag details <tag>       //shows info about tag
tag list                //shows all tags 
tag deleteuser <@user>  //deletes tags by user
tag delete <name>       //deletes tag
tag <name> <content>    //creates the tag
```
### tempban

Bans a user for a while

Aliases: tempban

Usable in public  channels

#### Usage

```php
tempban <user>     //Remove user from guild, unable to rejoin for a while user from guild
```
### template

adds/removes templates

Aliases: template, tpl

Usable in public and private channels

#### Usage

```php
template <keyphrase>                  //shows all templates for a keyphrase
template add <keyphrase> <text...>    //adds a template for keyphrase
template search <contains>            //searches for keyphrases matching part of the <contains>
template list <page>                  //lists all keyphrases
template remove <keyphrase> <index>   //removes selected template for keyphrase
template debug [on/off]               //enables/disabled debugging of templates

There are a few keywords you can utilize in templates. These keywords will be replaced by its value 
To see which variables are at your disposal:

template variable                    //all variables
template variable <keyphrase>        //variables for that keyphrase

for users with botadmin+, use 'template global ...' for global templates
```
### test

kaaz's test command

Aliases: test

Usable in public  channels

#### Usage

```php
NOPE
```
### todo

administer todo items

Aliases: todo

Usable in public and private channels

#### Usage

```php
todo                         //overview of your lists items
todo create                  //creates the list
todo list <name/code>        //check todo items of a list
todo add <text>              //adds a todo item to your list
todo remove <id>             //removes a todo item from your list
todo check <text>            //marks an item as checked
todo uncheck <text>          //marks an item as unchecked
todo clearchecked            //deletes checked items
todo priority <number> <priority>     //sets a priority of a todo item
```
### ud

A veritable cornucopia of streetwise lingo

Aliases: ud

Usable in public and private channels

#### Usage

```php
ud <anything>  //looks up what it means on urban dictionary
```
### uptime

How long am I running for?

Aliases: uptime

Usable in public and private channels
### user

Shows information about the user

Aliases: user, whois

Usable in public and private channels

#### Usage

```php
user                             //info about you
user @user                       //info about @user
user @user joindate yyyy-MM-dd   //overrides the join-date of a user
user @user joindate reset        //restores the original value
user guilds @user                //what guilds/shards @user most likely uses
```
### userrank

This command is intended for bot admins

Aliases: userrank, ur

Usable in public  channels

#### Usage

```php
userrank <user>                   //check rank of user
userrank <user> <rank>            //gives a rank to user
userrank <user> perm <+/-> <node> //adds/removes permission from user
userrank permlist                 //lists all permissions
userrank ranks                    //lists all ranks
```
### version

Shows what versions I'm using

Aliases: version, v

Usable in public and private channels

#### Usage

```php
version  //version usage
```
### volume

gets and sets the volume of the music

Aliases: volume, vol

Usable in public  channels

#### Usage

```php
volume              //shows current volume
volume <1 to 100>   //sets volume
```
### warn

Give a user a warning

Aliases: warn

Usable in public  channels

#### Usage

```php
warn <user>     //Adds a strike to the user user from guild
```



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


