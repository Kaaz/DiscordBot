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
[8ball](#8ball) | [autoreply](#autoreply) | [bank](#bank) | [blackjack](#blackjack) | [catfact](#catfact)
[changename](#changename) | [command](#command) | [config](#config) | [current](#current) | [exec](#exec)
[exit](#exit) | [game](#game) | [help](#help) | [importmusic](#importmusic) | [info](#info)
[invite](#invite) | [join](#join) | [joke](#joke) | [leave](#leave) | [leaveguild](#leaveguild)
[mcstatus](#mcstatus) | [ping](#ping) | [play](#play) | [playlist](#playlist) | [pm](#pm)
[poll](#poll) | [profile](#profile) | [purge](#purge) | [reboot](#reboot) | [reddit](#reddit)
[reload](#reload) | [report](#report) | [role](#role) | [roll](#roll) | [rotate](#rotate)
[say](#say) | [sendfile](#sendfile) | [skip](#skip) | [slot](#slot) | [stop](#stop)
[subscribe](#subscribe) | [system](#system) | [tag](#tag) | [template](#template) | [user](#user)
[userrank](#userrank) | [version](#version) | [volume](#volume) | 

## Games

Games can be accessed though the **!game** command

A list of games:

Key | Name | Players |
--- | --- | --- |
cf | Connect Four | 2
gos | Game of sticks | 2
tic | Tic tac toe | 2


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


## Per guild configuration

The per-guild configuration can be accessed though the **!config** command, to use it you must be the server owner, or have the discord-administrator permission
 
The following settings can be changed per guild:

Key | Default | Description |
--- | --- | ---|
auto_reply | false | use the auto reply feature?<br/>Looks for patterns in messages and replies to them (with a cooldown)<br/>true -> enable auto replying to matched messages<br/>true -> disable auto replying
bot_channel | general | Channel where the bots default output goes to
bot_listen | all | What channels to listen to? (all;mine)<br/>all -> responds to all channels<br/>mine -> only responds to messages in configured channel
bot_update_warning | playing | Show a warning that there is an update and that the bot will be updating soon.<br/>always  -> always show the message in the bot's configured default channel<br/>playing -> only announce when the bot is playing music and in the bot's configured music channel<br/>off     -> don't announce when the bot is going down for an update
chat_bot_enabled | false | Chat with people
cleanup_messages | no | Delete messages after a while? (yes;no;nonstandard)<br/>yes -> Always delete messages<br/>no -> Never delete messages<br/>nonstandard -> delete messages outside of bot's default channel
command_prefix | $ | Prefix for commands (between 1 and 3 characters)
help_in_pm | false | show help in a private message?<br/>true  -> send a message to the user requesting help<br/>false -> output help to the channel where requested
module_games | true | Let people play games against each other
music_channel | music | Channel where the bots music-related output goes to
music_channel_title | false | Updates the music channel's topic with the currently playing song<br/>true  -> yes change the topic at the beginning of every song<br/>false -> leave the channel topic title alone!
music_playing_message | clear | Clear the now playing message?<br/>clear  -> sends a message and deletes it when the song is over or skipped<br/>normal -> send the message and just leave it be<br/>off    -> don't send now playing messages
music_show_listeners | true | Show who's listening in the *current* command<br/>true  -> List all the people who are currently listening to music<br/>false -> Don't show listeners
music_volume | 10 | sets the default volume of the music player<br/>So the next time the bot connects it starts with this volume<br/><br/>Accepts a value between 0 and 100
pm_user_events | false | Send a private message to owner when something happens to a user?<br/>true  -> sends a private message to guild-owner<br/>false -> does absolutely nothing
show_unknown_commands | false | Show message on nonexistent commands<br/>true -> returns a help message<br/>false -> stays silent
use_economy | false | Use the economy feature?<br/>false -> nope!<br/>true -> yep!
user_time_ranks | false | This setting will require me to have the manage role permission!<br/>Users are given a role based on their time spend in the discord server<br/>If you'd like to use the time based ranks, be sure to check out the other settings first!<br/>Setting:  Use time based ranks?<br/>true  -> yes<br/>false -> no
user_time_ranks_notify | no | Send a notification whenever a user goes up a rank?<br/>no      -> Don't notify anyone, stay silent!<br/>private -> send a private message to the user who ranked up<br/>public  -> announce it in a channel<br/>both    -> perform both private and public actions 
user_time_ranks_prefix | [rank] | The prefix of the role name for the time based role ranking<br/>Using this prefix to manage roles so make sure its somewhat unique! Or you'll have to cleanup yourself :)<br/>If you'd like to use the time based ranks make sure to set this first!<br/><br/>The prefix can be between 3 and 8 in length
welcome_new_users | false | Show a welcome message to new users?<br/>true  -> shows a welcome message to new users<br/>false -> stays silent



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
Spectator | 0 seconds 
Outsider | about an hour 
Lurker | 3 hours 
Prospect | 23 hours 
Friendly | 2 days 
Regular | 6 days 
Honored | 13 days 
Veteran | 29 days 
Revered | 59 days 
Herald | 89 days 
Exalted | 179 days 


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

Patterns where the bot auto-replies to. 

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
### bank

For all your banking needs

Accessible though: bank, currency, money

Usable in public and private channels

#### Usage

```php
bank                       //shows current balance
bank history               //shows last transactions
bank donate @user <amount> //donates <amount> to @user 
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

Add and remove custom commands

Accessible though: command

Usable in public and private channels

#### Usage

```php
command add <command> <action>  //adds a command
command delete <command>        //deletes a command
command list                    //shows a list of existing custom commands
```
### config

Gets/sets the configuration of the bot

Accessible though: config, setting

Usable in public  channels

#### Usage

```php
config                    //overview
config <property>         //check details of property
config <property> <value> //sets property
```
### current

retrieves information about the song currently playing

Accessible though: current, playing, np, nowplaying

Usable in public  channels

#### Usage

```php
current               //info about the currently playing song
current title <title> //sets title of current song
current ban           //bans the current track from being randomly played
current artist        //sets the artist of current song
current correct       //accept the systems suggestion of title/artist
current reversed      //accept the systems suggestion in reverse [title=artist,artist=title]
```
### exec

executes commandline stuff

Accessible though: exec

Usable in public and private channels
### exit

completely shuts the bot down

Accessible though: exit, brexit

Usable in public and private channels
### game

play games against eachother!

Accessible though: game

Usable in public  channels

#### Usage

```php
game list                 //to see a list games
game <@user> <gamecode>   //play a game against @user
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
### leave

Leaves the voicechannel

Accessible though: leave

Usable in public  channels
### leaveguild

leaves guild :(

Accessible though: leaveguild

Usable in public and private channels

#### Usage

```php
leaveguild     //leaves the guild
```
### mcstatus

Shows some information about the server

Accessible though: mcstatus

Usable in public  channels

#### Usage

```php
mcstatus <serverip>
mcstatus <serverip> <serverport> 
```
### ping

checks the latency of the bot

Accessible though: ping

Usable in public and private channels
### play

Plays a song from youtube

Accessible though: play

Usable in public  channels

#### Usage

```php
play <youtubelink>    //download and plays song
play <part of title>  //shows search results
play                  //just start playing something
```
### playlist

information about the playlist/history

Accessible though: playlist

Usable in public  channels

#### Usage

```php
playlist          //playlist queue
playlist clear    //playlist queue
playlist history  //list of recently played songs
```
### pm

Send a message to user

Accessible though: pm

Usable in public and private channels

#### Usage

```php
pm <@user> <message..>
```
### poll

Strawpoll: propose a question and choices for the chat to vote on

Accessible though: poll

Usable in public  channels

#### Usage

```php
poll          //status of active poll 
poll create <question> ;<duration in minutes>;<option1>;<option2>;<etc.> 
              //creates a poll for the duration
poll 1-9      //vote on the options
```
### profile

Shows your profile in a fancy way

Accessible though: profile, avatar

Usable in public and private channels

#### Usage

```php
profile
profile <@user>  //shows the profile of @user
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
### role

Management of roles

Accessible though: role, roles

Usable in public  channels

#### Usage

```php
role                             //lists roles
role list                        //lists roles
role cleanup                     //cleans up the roles from the time-based rankings
role setup                       //creates the roles for the time-based rankings
role bind BOT_ROLE <discordrole> //binds a discordrole to a botrole
role add @user <role>            //adds role to user
role remove @user <role>         //remove role from user
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
### say

repeats you

Accessible though: say

Usable in public and private channels

#### Usage

```php
say <anything>
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
skip      //skips current track
skip perm //skips permanently; never hear this song again
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

Accessible though: stop

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
template list <page>                 //lists all keyphrases
template list <contains>     		  //lists all options for keyphrase
template remove <keyphrase> <index>  //removes selected template for keyphrase
template add <keyphrase> <text...>   //adds a template for keyphrase
```
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

User Ranks!

Accessible though: userrank, ur

Usable in public  channels

#### Usage

```php
userrank <user>        //list of tags
userrank <user> <rank> //shows your tags
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
