# DiscordBot

A Java bot for [Discord](https://discordapp.com/) using the [JDA library](https://github.com/DV8FromTheWorld/JDA).

It uses mysql to store data, a create script can be found in the sql folder  
To get music from youtube it makes use of [youtube-dl](https://github.com/rg3/youtube-dl) and [ffmpeg](https://www.ffmpeg.org/)
If you'd like to run your own version of this project check the [installation part](#run-the-bot-yourself)

You can play/test it on discord 
[![Discord](https://discordapp.com/api/guilds/225168913808228352/widget.png)](https://discord.gg/eaywDDt)
## What can it do?


* Respond to commands
* Add custom commands
* play/download music
* [customizable globally](#global-configuration) and [per guild](#per-guild-configuration)
* play games against other users
* subscription system
* various optional modules
* auto self updates
* [auto ranking system](#ranking-system)

### Commands

Commands are prefixed with a "!" by default, this can be configured.
For a list of commands in discord the **help** command can be used.
For more information about a command use **help \<commandname\>**

Current list of all available commands. See below for a more detailed list

Commands | | | | |
--- | --- | ---| ---| ---
[8ball](#8ball) | [autoreply](#autoreply) | [blackjack](#blackjack) | [catfact](#catfact) | [changename](#changename)
[command](#command) | [config](#config) | [consolecomm](#consolecomm) | [current](#current) | [exec](#exec)
[exit](#exit) | [fml](#fml) | [game](#game) | [getrole](#getrole) | [guildstats](#guildstats)
[help](#help) | [importmusic](#importmusic) | [info](#info) | [invite](#invite) | [join](#join)
[joke](#joke) | [leaveguild](#leaveguild) | [meme](#meme) | [pause](#pause) | [ping](#ping)
[play](#play) | [playlist](#playlist) | [pm](#pm) | [prefix](#prefix) | [purge](#purge)
[reboot](#reboot) | [reddit](#reddit) | [reload](#reload) | [report](#report) | [roleadmin](#roleadmin)
[roll](#roll) | [rotate](#rotate) | [sendfile](#sendfile) | [skip](#skip) | [slot](#slot)
[stop](#stop) | [subscribe](#subscribe) | [system](#system) | [tag](#tag) | [template](#template)
[ud](#ud) | [uptime](#uptime) | [user](#user) | [userrank](#userrank) | [version](#version)
[volume](#volume) | 

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

Key | Default | Description |
--- | --- | ---|
auto_reply | false | use the auto reply feature?<br/>Looks for patterns in messages and replies to them (with a cooldown)<br/>true -> enable auto replying to matched messages<br/>true -> disable auto replying
bot_channel | general | Channel where the bots default output goes to
bot_debug_templates | true | Show which templates are being used on places.<br/><br/>valid values: <br/>true       -> Shows the keyphrases being used <br/>false      -> Shows normal text <br/><br/>for instance if you don't have permission to access a command:<br/><br/>setting this to true would show:<br/>no_permission<br/><br/>false would show:<br/>You don't have permission to use that!
bot_listen | all | What channels to listen to? (all;mine)<br/>all -> responds to all channels<br/>mine -> only responds to messages in configured channel
bot_logging_channel | false | The channel where the logging of events happens. Such as users joining/leaving <br/><br/>Setting this to 'false' will disable it (without the quotes)<br/><br/>To enable it, set this setting to match the channel name where you want the logging to happen<br/>If you specify an invalid channel, this setting will disable itself
bot_update_warning | playing | Show a warning that there is an update and that the bot will be updating soon.<br/>always  -> always show the message in the bot's configured default channel<br/>playing -> only announce when the bot is playing music and in the bot's configured music channel<br/>off     -> don't announce when the bot is going down for an update
chat_bot_enabled | false | Chat with people
cleanup_messages | no | Delete messages after a while? (yes;no;nonstandard)<br/>yes -> Always delete messages<br/>no -> Never delete messages<br/>nonstandard -> delete messages outside of bot's default channel
command_prefix | $ | Prefix for commands (between 1 and 3 characters)
help_in_pm | false | show help in a private message?<br/>true  -> send a message to the user requesting help<br/>false -> output help to the channel where requested
module_games | true | Let people play games against each other
music_channel | music | Channel where the bots music-related output goes to
music_channel_auto | false | The channel where I automatically connect to if a user joins<br/><br/>false:<br/>Not using this setting, wont auto-connect to anything.<br/><br/>setting this to match a voice channel name:<br/>The moment a user connects to the specified channel I connect too and start to play music.<br/><br/>Important to note: <br/>* If the configured channel does not exist, this setting will be turned off<br/>* If I'm already connected to a different voice-channel I won't use this setting
music_channel_title | false | Updates the music channel's topic with the currently playing song<br/><br/>auto  -> update the title every 10 seconds with the track its playing<br/>true  -> yes change the topic at the beginning of every song<br/>false -> leave the channel topic title alone!
music_clear_admin_only | true | Only allow admins to clear the music queue?<br/><br/>true<br/>Only admins can clear the music queue<br/><br/>false<br/>Everyone can clear the queue
music_playing_message | clear | Clear the now playing message?<br/>clear  -> sends a message and deletes it when the song is over or skipped<br/>normal -> send the message and just leave it be<br/>off    -> don't send now playing messages
music_playlist_id | 0 | used to store the last used playlist 
music_queue_only | false | Stop playing music once the queue is empty?<br/><br/>true<br/>once the queue is empty I stop playing music and leave the voice channel<br/><br/>false<br/>If the queue is empty, I'm gonna pick the track.
music_role_requirement | none | In order to use music commands you need this role!<br/>Setting this value to none will disable the requirement
music_show_listeners | false | Show who's listening in the *current* command<br/>true  -> List all the people who are currently listening to music<br/>false -> Don't show listeners
music_skip_admin_only | false | Only allow admins to use the skip command?<br/><br/>true<br/>Only admins have permission to use the skip command<br/><br/>false<br/>Everyone can use the skip command
music_volume | 10 | sets the default volume of the music player<br/>So the next time the bot connects it starts with this volume<br/><br/>Accepts a value between 0 and 100
music_volume_admin | false | Require a guild admin to change the volume<br/><br/>false -> allow all users to change the bot's volume<br/>true -> only allow guild admins to change the bot's volume
music_vote_percent | 1 | Percentage of users (rounded down) required to skip the currently playing track<br/><br/>eg; when set to 25, and 5 listeners it would require 2 users to vote skip <br/><br/>Accepts a value between 1 and 100
pm_user_events | false | Send a private message to owner when something happens to a user?<br/>true  -> sends a private message to guild-owner<br/>false -> does absolutely nothing
show_unknown_commands | false | Show message on nonexistent commands and blacklisted commands<br/>true -> returns a help message<br/>false -> stays silent
use_economy | false | Use the economy feature?<br/>false -> nope!<br/>true -> yep!
user_time_ranks | false | This setting will require me to have the manage role permission!<br/>Users are given a role based on their time spend in the discord server<br/>If you'd like to use the time based ranks, be sure to check out the other settings first!<br/>Setting:  Use time based ranks?<br/>true  -> yes<br/>false -> no
user_time_ranks_notify | no | Send a notification whenever a user goes up a rank?<br/>no      -> Don't notify anyone, stay silent!<br/>false   -> Don't notify anyone, stay silent!<br/>private -> send a private message to the user who ranked up<br/>public  -> announce it in a channel<br/>both    -> perform both private and public actions 
user_time_ranks_prefix | [rank] | The prefix of the role name for the time based role ranking<br/>Using this prefix to manage roles so make sure its somewhat unique! Or you'll have to cleanup yourself :)<br/>If you'd like to use the time based ranks make sure to set this first!<br/><br/>The prefix can be between 3 and 8 in length
welcome_new_users | false | Show a welcome message to new users?<br/>Valid options:<br/>true  -> shows a welcome when a user joins or leaves the guild<br/>false -> Disabled, doesn't say anything<br/><br/>The welcome message can be set with the template: <br/>welcome_new_user<br/><br/>The welcome back message can be set with the template (if the user had joined before): <br/>welcome_back_user<br/><br/>The leave message can be set with the template: <br/>message_user_leaves<br/><br/>If multiple templates are set a random one will be chosen<br/>See the template command for more details



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
Revered | 60 days 
Herald | 90 days 
Exalted | 180 days 
Beloved | 365 days 
Favorite | 700 days 
Consul | 1000 days 


## Run the bot yourself

Before we get started I want to note that the development happens mainly on a windows machine, so there might be some unforeseen errors on other operating systems.  
If you do encounter errors, please let me know so I can fix them.  

Right, so lets get started. Before starting make sure you at least have the following installed:

* java 8
* git
* maven
* mysql

Database:  
execute the sql queries in the /sql/create.sql file

clone the project with git  
`git clone https://github.com/MaikWezinkhof/DiscordBot /path/to/project`  
Go to the root of the project and install the dependencies using maven  
`mvn install`  
Now build the project using maven  
`mvn clean process-resources compile assembly:single`  
In the target map there should be a file called Discordbot-VERSION_full.jar (where version is the latest version number)  
Move this file over to a location wherever you want to start the bot from.  

You can launch the bot with the following command:
`java -jar <jarfilename>`  

The first time It will generate an application.cfg file and exit.
You'll have to edit the config file and add in your token, database configuration, etc.

You will also have to get [youtube-dl](https://rg3.github.io/youtube-dl/download.html) and [ffmpeg](https://ffmpeg.zeranoe.com/builds/)

The Config setting `youtubedl_exe` has to point to the youtube-dl executable.
For ffmpeg, the binaries have to be either in the same directory as the bot.jar or be included in in the system path somewhere.




## Warning

This project is still in an early stage. Some things might not work or get changed drastically.

## Usage

On the first run it will generate a config file and stop running. You'll need to at least set the token and the property **bot_enabled** to true

## Command details

### 8ball

See what the magic 8ball has to say

Accessible though: 8ball

Usable in public and private channels
### autoreply

regular expression Patterns where the bot auto-replies to. 

Accessible though: autoreply, ar

Usable in public and private channels

#### Usage

```php
ar create <tagname>      //creates tag
ar regex <tag> <value>     //edit the regex of a tag
ar response <tag> <value>  //change the response of a reply
ar tag <tag> <value>       //change the tag of a reply
ar cd <tag> <value>        //change the cooldown (millis) of a reply
ar guild <tag> <guildid>   //guild of a tag, 0 for global
ar test <tag> <text>       //test for a match
ar delete <tag>            //deletes a tag
```
### blackjack

play a game of blackjack!

Accessible though: blackjack, bj

Usable in public and private channels

#### Usage

```php
blackjack        //check status
blackjack hit    //hits
blackjack stand  //stands
```
### catfact

Cat facts!

Accessible though: catfact

Usable in public and private channels
### changename

Changes my name

Accessible though: changename

Usable in public and private channels
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

Accessible though: command, cmd, commands, customcommand

Usable in public  channels

#### Usage

```php
command add <command> <action>  //adds a command
command delete <command>        //deletes a command
command                         //shows a list of existing custom commands
```
### config

Gets/sets the configuration of the bot

Accessible though: config, setting, cfg

Usable in public  channels

#### Usage

```php
config                    //overview
config <property>         //check details of property
config <property> <value> //sets property

config reset yesimsure    //resets the configuration to the default settings
```
### consolecomm

Sets the communication channel of the console input

Accessible though: consolecomm

Usable in public  channels

#### Usage

```php
consolecomm connect     //connects to current channel
consolecomm disconnect  //disconnects from current
```
### current

retrieves information about the song currently playing

Accessible though: current, playing, np, nowplaying

Usable in public  channels

#### Usage

```php
current                 //info about the currently playing song
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
### exec

executes commandline stuff

Accessible though: exec

Usable in public and private channels
### exit

completely shuts the bot down

Accessible though: exit, brexit

Usable in public and private channels
### fml

fmylife! Returns a random entry from fmylife.com

Accessible though: fml

Usable in public and private channels
### game

play games against eachother!

Accessible though: game

Usable in public  channels

#### Usage

```php
game list                 //to see a list games
game <@user> <gamecode>   //play a game against @user
game cancel               //cancel an active game!
```
### getrole

allows users to request a role

Accessible though: getrole

Usable in public  channels

#### Usage

```php
list                //see what roles are available
remove <rolename>   //removes the <rolename> from you
<rolename>          //assign the <rolename> to you 
```
### guildstats

shows some statistics

Accessible though: guildstats, stats

Usable in public and private channels

#### Usage

```php
stats         //stats!
stats mini    //minified!
stats users   //graph of when users joined!
```
### help

An attempt to help out

Accessible though: help, ?, halp, helpme, h

Usable in public and private channels

#### Usage

```php
help            //index of all commands
help <command>  //usage for that command
```
### importmusic

Special command for special use case. Imports music files from a directory. Only imports files with a valid id3v[1-2] tag

Accessible though: importmusic

Usable in public and private channels

#### Usage

```php
importmusic <path/to/music>  //imports a folder
```
### info

Shows some general information about me and my future plans.

Accessible though: info, about

Usable in public and private channels

#### Usage

```php
info          //general info
info planned  //see whats planned in the near future
info bugs     //known bugs
info progress //see whats currently being worked on
```
### invite

Provides an invite link to add the bot to your server.

Accessible though: invite, inv

Usable in public and private channels
### join

joins a voicechannel

Accessible though: join

Usable in public  channels

#### Usage

```php
join                //attempts to join you
join <channelname>  //attempts to join channelname
```
### joke

An attempt to be funny

Accessible though: joke

Usable in public and private channels
### leaveguild

leaves guild :(

Accessible though: leaveguild

Usable in public and private channels

#### Usage

```php
leaveguild     //leaves the guild
```
### meme

generate a meme!

Accessible though: meme

Usable in public and private channels

#### Usage

```php
meme type                               //list of all valid types
meme <type> <toptext> || <bottomtext>   //make the meme!

example: 
meme sohappy If I could use this meme || I would be so happy
```
### pause

pauses the music or resumes it if its paused

Accessible though: pause, resume

Usable in public  channels
### ping

checks the latency of the bot

Accessible though: ping

Usable in public and private channels
### play

Plays a song from youtube

Accessible though: play, music, p, m

Usable in public  channels

#### Usage

```php
play <youtubelink>    //download and plays song
play <part of title>  //shows search results
play                  //just start playing something
```
### playlist

information about the playlists

Accessible though: playlist, pl

Usable in public  channels

#### Usage

```php
-- using playlists 
playlist guild                       //use the guild's playlist
playlist global                      //use the global playlist
playlist settings                    //check the settings for the active playlist
playlist                             //info about the current playlist
playlist list <pagenumber>           //Shows the music in the playlist

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

Accessible though: pm

Usable in public and private channels

#### Usage

```php
pm <@user> <message..>
```
### prefix

Forgot what the prefix is? I got you covered

Accessible though: prefix

Usable in public and private channels

#### Usage

```php
prefix                           //shows the set prefix
prefix <prefix>                  //sets the prefix to <prefix>
```
### purge

deletes non-pinned messages

Accessible though: purge, clear, delete

Usable in public  channels

#### Usage

```php
purge               //deletes up to 100 messages
purge <limit>       //deletes non-pinned messages
purge @user         //deletes messages from user
purge @user <limit> //deletes up to <limit> messages from user
purge commands      //delete command related messages
purge emily         //deletes my messages :(
```
### reboot

restarts the bot

Accessible though: reboot, restart

Usable in public and private channels

#### Usage

```php
reboot         //reboots the system
reboot update  //reboots the system and updates
```
### reddit

Posts something from reddit

Accessible though: reddit, r

Usable in public and private channels

#### Usage

```php
r <subreddit>
```
### reload

reloads the configuration

Accessible though: reload

Usable in public and private channels
### report

Report bugs/abuse/incidents

Accessible though: report

Usable in private channels

#### Usage

```php
report <subject> | <message..>
```
### roleadmin

Management of roles & general permissions 
You can give users the ability to self-assign roles. 
Users can get/remove their own roles with the `getrole` command 

Accessible though: roleadmin, ra

Usable in public  channels

#### Usage

```php
You can specify which roles are self-assignable by users with the following commands: 

roleadmin self                                 //check what roles are self-assignable
roleadmin self add <role>                      //add a role to the list of assignable roles
roleadmin self remove <role>                   //remove a role from the list of assignable roles


roleadmin                        //lists roles
roleadmin cleanup                //cleans up the roles from the time-based rankings
roleadmin setup                  //creates the roles for the time-based rankings
```
### roll

if you ever need a random number

Accessible though: roll, dice, rng

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

Accessible though: rotate

Usable in public and private channels

#### Usage

```php
rotate <text..> 
```
### sendfile

executes commandline stuff

Accessible though: sendfile

Usable in public and private channels
### skip

skip current track

Accessible though: skip, next

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

Accessible though: slot

Usable in public and private channels

#### Usage

```php
slot      //play
slot play //play the game
slot info //info about payout
```
### stop

stops playing music

Accessible though: stop, leave

Usable in public  channels
### subscribe

subscribe the channel to certain events

Accessible though: subscribe, sub

Usable in public  channels

#### Usage

```php
subscribe                //check what subscriptions are active
subscribe <name>         //subscribe to subject
subscribe stop <name>    //stop subscription to subject
subscribe info <name>    //information about subject
subscribe list           //See what subscription options there are
```
### system

Shows memory usage

Accessible though: system, sysinfo, sys

Usable in public and private channels
### tag

Tags!

Accessible though: tag, t

Usable in public  channels

#### Usage

```php
tag                  //list of tags
tag <name>           //shows the tag
tag mine             //shows your tags
tag list             //shows all tags 
tag delete <name>    //deletes tag
tag <name> <content> //creates the tag
```
### template

adds/removes templates

Accessible though: template, tpl

Usable in public and private channels

#### Usage

```php
template <keyphrase>                  //shows all templates for a keyphrase
template add <keyphrase> <text...>    //adds a template for keyphrase
template search <contains>            //searches for keyphrases matching part of the <contains>
template list <page>                  //lists all keyphrases
template remove <keyphrase> <index>   //removes selected template for keyphrase

There are a few keywords you can utilize in templates. These keywords will be replaced by its value 

Key                Replacement
---                ---
%user%             Username 
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
```
### ud

A veritable cornucopia of streetwise lingo

Accessible though: ud

Usable in public and private channels

#### Usage

```php
ud <anything>  //looks up what it means on urban dictionary
```
### uptime

How long am I running for?

Accessible though: uptime

Usable in public and private channels
### user

Shows information about the user

Accessible though: user, whois

Usable in public and private channels

#### Usage

```php
user         //info about you
user @user   //info about @user
```
### userrank

This command is intended for bot admins

Accessible though: userrank, ur

Usable in public  channels

#### Usage

```php
userrank <user>                   //check rank of user
userrank <user> <rank>            //gives a rank to user
userrank <user> perm <+/-> <node> //adds/removes permission from user
userrank permlist                 //lists all permissions
```
### version

Shows what versions I'm using

Accessible though: version, v

Usable in public and private channels

#### Usage

```php
version  //version usage
```
### volume

gets and sets the volume of the music

Accessible though: volume, vol

Usable in public  channels

#### Usage

```php
volume              //shows current volume
volume <1 to 100>   //sets volume
```



## Global configuration

The global configuration is stored in the application.cfg file, which is generated the first time you run the application

The following settings can be set globally:

Setting name | default | description
---|---|---
BOT_ENABLED | false | Enables the bot<br/> This must be set to true in order to run the bot
BOT_AUTO_UPDATE | false | Enable automatic updates. <br/>For this to work you'll have to launch the bot though my other project<br/>https://github.com/MaikWezinkhof/ConfigurationBuilder
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
DB_HOST | "localhost" | mysql hostname
DB_USER | "root" | mysql user
DB_PASS | "" | mysql password
DB_NAME | "discord" | mysql database name
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


