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
* [auto ranking system](#ranking-system)

### Commands

Commands are prefixed with a "!" by default, this can be configured.
For a list of commands in discord the **help** command can be used.
For more information about a command use **help \<commandname\>**

Current list of all available commands. See below for a more detailed list

Commands | | | | |
--- | --- | ---| ---| ---
[8ball](#8ball) | [avatar](#avatar) | [blackjack](#blackjack) | [catfact](#catfact) | [changename](#changename)
[command](#command) | [config](#config) | [current](#current) | [exit](#exit) | [game](#game)
[help](#help) | [info](#info) | [invite](#invite) | [join](#join) | [joke](#joke)
[leave](#leave) | [play](#play) | [playlist](#playlist) | [pm](#pm) | [poec](#poec)
[poeitem](#poeitem) | [poelab](#poelab) | [poll](#poll) | [purge](#purge) | [r](#r)
[reload](#reload) | [role](#role) | [roll](#roll) | [rotate](#rotate) | [say](#say)
[skip](#skip) | [slot](#slot) | [stop](#stop) | [subscribe](#subscribe) | [system](#system)
[template](#template) | [user](#user) | [volume](#volume) | 

## Games

Games can be accessed though the **!game** command

A list of games:

Key | Name | Players |
--- | --- | --- |
cf | Connect Four | 2
gos | Game of sticks | 2
tic | Tic tac toe | 2



## Per guild configuration

The configuration can be accessed though the **!config** command
 
The following settings can be changed per guild:

Key | Default | Description |
--- | --- | ---|
bot_channel | general | Channel where the bots default output goes to
bot_listen | all | What channels to listen to? (all;mine)<br/>all -> responds to all channels<br/>mine -> only responds to messages in configured channel
chat_bot_enabled | false | Chat with people
cleanup_messages | no | Delete messages after a while? (yes;no;nonstandard)<br/>yes -> Always delete messages<br/>no -> Never delete messages<br/>nonstandard -> delete messages outside of bot's default channel
command_prefix | $ | Prefix for commands (between 1 and 3 characters)
module_games | true | Let people play games against each other
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



## Warning

This project is still in an early stage. Some things might not work or get changed drastically.

## Usage

On the first run it will generate a config file and stop running. You'll need to at least set the token and the property **bot_enabled** to true

## Command details

### 8ball

See what the magic 8ball has to say

Usable in public and private channels
### avatar

Changes my avatar

Usable in public and private channels
### blackjack

play a game of blackjack!

Usable in public and private channels

#### Usage

```php
blackjack        //check status
blackjack hit    //hits
blackjack stand  //stands
```
### catfact

Cat facts!

Usable in public and private channels
### changename

Changes my name

Usable in public and private channels
### command

Add and remove custom commands

Usable in public and private channels

#### Usage

```php
command add <command> <action>  //adds a command
command delete <command>        //deletes a command
command list                    //shows a list of existing custom commands
```
### config

Gets/sets the configuration of the bot

Usable in public ~~and private~~ channels

#### Usage

```php
config                    //overview
config <property>         //check details of property
config <property> <value> //sets property
```
### current

retrieves information about the song currently playing

Usable in public ~~and private~~ channels

#### Usage

```php
current               //info about the currently playing song
current title <title> //sets title of current song
current ban           //bans the current track from being randomly played
current artist        //sets the artist of current song
current correct       //accept the systems suggestion of title/artist
current reversed      //accept the systems suggestion in reverse [title=artist,artist=title]
```
### exit

completely shuts the bot down

Usable in public and private channels
### game

play games against eachother!

Usable in public ~~and private~~ channels

#### Usage

```php
game list                 //to see a list games
game <@user> <gamecode>   //play a game against @user
```
### help

An attempt to help out

Usable in public and private channels

#### Usage

```php
help            //index of all commands
help <command>  //usage for that command
```
### info

Shows some general information about me and my future plans.

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

Usable in public and private channels
### join

joins a voicechannel

Usable in public ~~and private~~ channels

#### Usage

```php
join                //attempts to join you
join <channelname>  //attempts to join channelname
```
### joke

An attempt to be funny

Usable in public and private channels
### leave

Leaves the voicechannel

Usable in public ~~and private~~ channels
### play

Plays a song from youtube

Usable in public ~~and private~~ channels

#### Usage

```php
play <youtubelink>       //download and plays song
play <soundcloudlink>    //download and plays song
play <youtubevideocode>  //download and plays song
play <part of title>     //shows search results
play <resultnumber>      //add result # to the queue
```
### playlist

information about the playlist/history

Usable in public ~~and private~~ channels

#### Usage

```php
playlist          //playlist queue
playlist clear    //playlist queue
playlist history  //list of recently played songs
```
### pm

Send a message to user

Usable in public and private channels

#### Usage

```php
pm <@user> <message..>
```
### poec

Returns a list of currency on your account

Usable in public and private channels

#### Usage

```php
poec                   //returns list of currency for default league
poec token <token>     //sets the session token
poec league <league>   //currency for league
```
### poeitem

Analyzes an item from path of exile.

Usable in public and private channels
### poelab

Attempts to find a description from reddit for the Labyrinth instance.

Usable in public and private channels

#### Usage

```php
poelab              //lists for all difficulties
poelab <difficulty> //only for that difficulty
```
### poll

Strawpoll: propose a question and choices for the chat to vote on

Usable in public ~~and private~~ channels

#### Usage

```php
poll          //status of active poll 
poll create <question> ;<duration in minutes>;<option1>;<option2>;<etc.> 
              //creates a poll for the duration
poll 1-9      //vote on the options
```
### purge

purges messages

Aliases: clear
Usable in public ~~and private~~ channels

#### Usage

```php
purge       //deletes non-pinned messages
purge @user //deletes messages from user
purge nova  //deletes my messages :(
```
### r

Posts something from reddit

Usable in public and private channels

#### Usage

```php
r <subreddit>
```
### reload

reloads the configuration

Usable in public and private channels
### role

Role

Usable in public ~~and private~~ channels

#### Usage

```php
role                     //lists roles
role list                //lists roles
role cleanup             //cleans up the roles from the time-based rankings
role setup               //creates the roles for the time-based rankings
role add @user <role>    //adds role to user
role remove @user <role> //remove role from user
```
### roll

if you ever need a random number

Aliases: dice, rng
Usable in public and private channels

#### Usage

```php
roll   //random number 1-6
roll <max>   //random number 1-<max>
roll <min> <max>   //random number <min>-<max>
```
### rotate

Rotate text!

Usable in public and private channels

#### Usage

```php
rotate <text..> 
```
### say

repeats you

Usable in public and private channels

#### Usage

```php
say <anything>
```
### skip

skip current track

Usable in public ~~and private~~ channels

#### Usage

```php
skip      //skips current track
skip perm //skips permanently; never hear this song again
```
### slot

Feeling lucky? try the slotmachine! You might just win a hand full of air!

Usable in public and private channels

#### Usage

```php
slot      //displays info and payout table
slot play //plays the game
```
### stop

stops playing music

Usable in public ~~and private~~ channels
### subscribe

subscribe the channel to certain events

Usable in public ~~and private~~ channels

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

Usable in public and private channels
### template

adds/removes templates

Usable in public and private channels

#### Usage

```php
template list                        //lists all keyphrases
template list <keyphrase>            //lists all options for keyphrase
template remove <keyphrase> <index>  //removes selected template for keyphrase
template add <keyphrase> <text...>   //adds a template for keyphrase
template toggledebug                 //shows keyphrases instead of text
```
### user

Shows information about the user

Usable in public and private channels

#### Usage

```php
user         //info about you
user @user   //info about @user
```
### volume

gets and sets the volume of the music

Usable in public ~~and private~~ channels

#### Usage

```php
volume              //shows current volume
volume <1 to 100>   //sets volume
```
