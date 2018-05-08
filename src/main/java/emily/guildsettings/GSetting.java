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

package emily.guildsettings;

import emily.guildsettings.types.EnumSettingType;
import emily.guildsettings.types.NumberBetweenSettingType;
import emily.guildsettings.types.StringLengthSettingType;
import emily.main.BotConfig;
import net.dv8tion.jda.core.entities.Guild;

import java.util.Collections;
import java.util.HashSet;

public enum GSetting {
    //General settings
    AUTO_REPLY("false", GuildSettingType.TOGGLE,
            "use the auto reply feature?\n" +
                    "Looks for patterns in messages and replies to them (with a cooldown)\n" +
                    "true -> enable auto replying to matched messages\n" +
                    "false -> disable auto replying",
            GSettingTag.MODERATION),
    BOT_LANGUAGE("en", new EnumSettingType("en", "nl", "de"),
            "The output language of the bot"),
    BOT_CHANNEL("general", GuildSettingType.TEXT_CHANNEL_MANDATORY,
            "Channel where the bots default output goes to",
            GSettingTag.CHANNEL),
    BOT_ADMIN_ROLE("", GuildSettingType.ROLE_OPTIONAL,
            "Users with this role are considered admins for the bot",
            GSettingTag.ROLE),
    SHOW_TEMPLATES(BotConfig.SHOW_KEYPHRASE ? "true" : "false", GuildSettingType.TOGGLE,
            "Show which templates are being used on places.\n\n" +
                    "valid values: \n" +
                    "true       -> Shows the keyphrases being used\n " +
                    "false      -> Shows normal text \n\n" +
                    "for instance if you don't have permission to access a command:\n\n" +
                    "setting this to true would show:\n" +
                    "no_permission\n\n" +
                    "false would show:\n" +
                    "You don't have permission to use that!",
            GSettingTag.DEBUG),
    DEBUG("false", GuildSettingType.TOGGLE,
            "Show some debug information.\n\n" +
                    "valid values: \n" +
                    "true       -> Show a lot more additional information\n " +
                    "false      -> don't \n\n" +
                    "If you want to check if certain things are (not) working.\n\n",
            GSettingTag.DEBUG),
    BOT_UPDATE_WARNING("playing", new EnumSettingType("always", "playing", "off"),
            "Show a warning that there is an update and that the bot will be updating soon.\n" +
                    "always  -> always show the message in the bot's configured default channel\n" +
                    "playing -> only announce when the bot is playing music and in the bot's configured music channel\n" +
                    "off     -> don't announce when the bot is going down for an update",
            GSettingTag.META),
    CLEANUP_MESSAGES("no", new EnumSettingType("yes", "no", "nonstandard"),
            "Delete messages after a while?\n" +
                    "yes         -> Always delete messages\n" +
                    "no          -> Never delete messages\n" +
                    "nonstandard -> delete messages outside of bot's default channel",
            GSettingTag.MODERATION, GSettingTag.META),
    COMMAND_PREFIX(BotConfig.BOT_COMMAND_PREFIX, new StringLengthSettingType(1, 4),
            "Prefix for commands (between 1 and 4 characters)",
            GSettingTag.COMMAND, GSettingTag.MODERATION),
    CHAT_BOT_ENABLED("false", GuildSettingType.TOGGLE,
            "Setting this to true will make it so that it responds to every message in the configured bot_channel",
            GSettingTag.META),
    COMMAND_LOGGING_CHANNEL("false", GuildSettingType.TEXT_CHANNEL_OPTIONAL,
            "The channel command usage will be logged to\n\n" +
                    "Example output:\n" +
                    "Kaaz#9436 has used `say` in #general\n" +
                    "arguments: this is not a test\n" +
                    "output: this is not a test\n\n" +
                    "Setting this to 'false' will disable it (without the quotes)\n" +
                    "To enable it, set this setting to match the channel name where you want the command logging to happen\n" +
                    "If you specify an invalid channel, this setting will disable itself",
            GSettingTag.COMMAND, GSettingTag.LOGGING, GSettingTag.CHANNEL),
    BOT_LOGGING_CHANNEL("false", GuildSettingType.TEXT_CHANNEL_OPTIONAL,
            "The channel where the logging of events happens. Such as users joining/leaving\n\n" +
                    "Setting this to 'false' will disable it (without the quotes)\n\n" +
                    "To enable it, set this setting to match the channel name where you want the logging to happen\n" +
                    "If you specify an invalid channel, this setting will disable itself",
            GSettingTag.CHANNEL, GSettingTag.LOGGING),
    BOT_MODLOG_CHANNEL("false", GuildSettingType.TEXT_CHANNEL_OPTIONAL,
            "The channel where mod-logging happens.\n" +
                    "A case will appear if a user has been banned/kicked/warned/muted\n\n" +
                    "Setting this to 'false' will disable it (without the quotes)\n\n" +
                    "To enable it, set this setting to match the channel name where you want the moderation-cases to go\n" +
                    "If you specify an invalid channel, this setting will disable itself",
            GSettingTag.CHANNEL, GSettingTag.LOGGING),
    BOT_MUTE_ROLE("false", GuildSettingType.ROLE_OPTIONAL,
            "This is the role which is applied to those who you use the mute command on\n\n" +
                    "Setting this value to false will disable the role applied with the mute command",
            GSettingTag.MODERATION, GSettingTag.ROLE),
    MODULE_GAMES("true", GuildSettingType.TOGGLE,
            "Let people play games against each other", GSettingTag.META),
    MODULE_ECONOMY("true", GuildSettingType.TOGGLE,
            "Use the economy feature?\n" +
                    "false -> nope!\n" +
                    "true -> yep!", GSettingTag.META),
    HELP_IN_PM("false", GuildSettingType.TOGGLE,
            "show help in a private message?\n" +
                    "true  -> send a message to the user requesting help\n" +
                    "false -> output help to the channel where requested",
            GSettingTag.META),
    PM_USER_EVENTS("false", GuildSettingType.TOGGLE,
            "Send a private message to owner when something happens to a user?\n" +
                    "true  -> sends a private message to guild-owner\n" +
                    "false -> does absolutely nothing",
            GSettingTag.META, GSettingTag.MODERATION),
    USER_TIME_RANKS("false", GuildSettingType.TOGGLE,
            "This setting will require me to have the manage role permission!\n" +
                    "Users are given a role based on their time spend in the discord server\n" +
                    "If you'd like to use the time based ranks, be sure to check out the other settings first!\n" +
                    "Setting:  Use time based ranks?\n" +
                    "true  -> yes\n" +
                    "false -> no",
            GSettingTag.META, GSettingTag.MODERATION),
    USER_TIME_RANKS_PREFIX("[rank]", new StringLengthSettingType(3, 8),
            "The prefix of the role name for the time based role ranking\n" +
                    "Using this prefix to manage roles so make sure its somewhat unique! Or you'll have to cleanup yourself :)\n" +
                    "If you'd like to use the time based ranks make sure to set this first!\n\n" +
                    "The prefix can be between 3 and 8 in length",
            GSettingTag.META, GSettingTag.MODERATION),
    SHOW_UNKNOWN_COMMANDS("false", GuildSettingType.TOGGLE,
            "Show message on nonexistent commands and blacklisted commands\n" +
                    "true -> returns a help message\n" +
                    "false -> stays silent",
            GSettingTag.META),
    WELCOME_NEW_USERS("false", GuildSettingType.TOGGLE,
            "Show a welcome message to new users?\n" +
                    "Valid options:\n" +
                    "true  -> shows a welcome when a user joins or leaves the guild\n" +
                    "false -> Disabled, doesn't say anything\n\n" +
                    "The welcome message can be set with the template: \n" +
                    "welcome_new_user\n\n" +
                    "The welcome back message can be set with the template (if the user had joined before): \n" +
                    "welcome_back_user\n\n" +
                    "The leave message can be set with the template: \n" +
                    "message_user_leaves\n\n" +
                    "If multiple templates are set a random one will be chosen\n" +
                    "See the template command for more details",
            GSettingTag.META),

    //Music related stuff
    MUSIC_CHANNEL_AUTO("false", GuildSettingType.VOICE_CHANNEL_OPTIONAL,
            "The channel where I automatically connect to if a user joins\n\n" +
                    "false:\n" +
                    "Not using this setting, wont auto-connect to anything.\n\n" +
                    "setting this to match a voice channel name:\n" +
                    "The moment a user connects to the specified channel I connect too and start to play music.\n\n" +
                    "Important to note: \n" +
                    "* If the configured channel does not exist, this setting will be turned off\n" +
                    "* If I'm already connected to a different voice-channel I won't use this setting\n",
            GSettingTag.CHANNEL, GSettingTag.MUSIC),
    MUSIC_CHANNEL("false", GuildSettingType.TEXT_CHANNEL_MANDATORY,
            "Channel where the bots music-related output goes to",
            GSettingTag.MUSIC, GSettingTag.CHANNEL),
    MUSIC_CHANNEL_TITLE("false", new EnumSettingType("auto", "true", "false"),
            "Updates the music channel's topic with the currently playing song\n\n" +
                    "auto  -> update the title every 10 seconds with the track its playing\n" +
                    "true  -> yes change the topic at the beginning of every song\n" +
                    "false -> leave the channel topic title alone!",
            GSettingTag.MUSIC, GSettingTag.CHANNEL),
    MUSIC_CLEAR_ADMIN_ONLY("true", GuildSettingType.TOGGLE,
            "Only allow admins to clear the music add?\n\n" +
                    "true\n" +
                    "Only admins can clear the music add\n\n" +
                    "false\n" +
                    "Everyone can clear the add"
            , GSettingTag.MUSIC, GSettingTag.ADMIN),
    MUSIC_PLAYLIST_ID("0", GuildSettingType.INTERNAL,
            "stores the last used playlist",
            GSettingTag.INTERNAL),
    MUSIC_PLAYING_MESSAGE("clear", new EnumSettingType("clear", "normal", "off"),
            "Clear the now playing message?\n" +
                    "clear  -> sends a message and deletes it when the song is over or skipped\n" +
                    "normal -> send the message and just leave it be\n" +
                    "off    -> don't send now playing messages",
            GSettingTag.MUSIC),
    MUSIC_QUEUE_ONLY("false", GuildSettingType.TOGGLE,
            "Stop playing music once the add is empty?\n\n" +
                    "true\n" +
                    "once the add is empty I stop playing music and leave the voice channel\n\n" +
                    "false\n" +
                    "If the add is empty, I'm gonna pick the track.",
            GSettingTag.MUSIC),
    MUSIC_RESULT_PICKER("1", new NumberBetweenSettingType(1, 5),
            "the amount of results the `play` command returns\n\n" +
                    "If its set to 1, it will always use the first result (no manual choice)\n\n" +
                    "If its set higher (max 5) it will respond with reactions where each button is a choice\n" +
                    "Note: This setting does require the add reactions permission",
            GSettingTag.MUSIC),
    MUSIC_ROLE_REQUIREMENT("false", GuildSettingType.ROLE_OPTIONAL,
            "In order to use music commands you need this role!\n" +
                    "Setting this value to false will disable the requirement",
            GSettingTag.MUSIC, GSettingTag.ROLE),
    MUSIC_SHOW_LISTENERS("false", GuildSettingType.TOGGLE,
            "Show who's listening in the *current* command\n" +
                    "true  -> List all the people who are currently listening to music\n" +
                    "false -> Don't show listeners",
            GSettingTag.MUSIC),
    MUSIC_SKIP_ADMIN_ONLY("false", GuildSettingType.TOGGLE,
            "Only allow admins to use the skip command?\n\n" +
                    "true\n" +
                    "Only admins have permission to use the skip command\n\n" +
                    "false\n" +
                    "Everyone can use the skip command",
            GSettingTag.MUSIC, GSettingTag.ADMIN),
    MUSIC_VOLUME("100", GuildSettingType.VOLUME,
            "sets the default volume of the music player\n" +
                    "So the next time the bot connects it starts with this volume\n\n" +
                    "Accepts a value between 0 and 100",
            GSettingTag.MUSIC),
    MUSIC_VOLUME_ADMIN("false", GuildSettingType.TOGGLE,
            "Require a guild admin to change the volume\n\n" +
                    "true -> only allow guild admins to change the bot's volume\n" +
                    "false -> allow all users to change the bot's volume",
            GSettingTag.MODERATION, GSettingTag.MUSIC),
    MUSIC_VOTE_PERCENT("40", GuildSettingType.PERCENTAGE,
            "Percentage of users (rounded down) required to skip the currently playing track\n\n" +
                    "eg; when set to 25, and 5 listeners it would require 2 users to vote skip\n\n" +
                    "Accepts a value between 1 and 100",
            GSettingTag.MUSIC),;

    private final String defaultValue;
    private final IGuildSettingType settingType;
    private final String description;
    private final HashSet<GSettingTag> tags;

    GSetting(String defaultValue, IGuildSettingType settingType, String description, GSettingTag... tags) {

        this.defaultValue = defaultValue;
        this.settingType = settingType;
        this.description = description;
        this.tags = new HashSet<>();
        Collections.addAll(this.tags, tags);
    }

    public boolean isInternal() {
        return tags.contains(GSettingTag.INTERNAL);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasTag(String tag) {
        return tags.contains(GSettingTag.valueOf(tag));
    }

    public boolean hasTag(GSettingTag tag) {
        return tags.contains(tag);
    }

    public HashSet<GSettingTag> getTags() {
        return tags;
    }

    /**
     * Checks if the value is a valid setting
     *
     * @param input value to check
     * @return is it a valid value
     */
    public boolean isValidValue(Guild guild, String input) {
        return settingType.validate(guild, input);
    }

    public String getValue(Guild guild, String input) {
        return settingType.fromInput(guild, input);
    }

    public String toDisplay(Guild guild, String value) {
        return settingType.toDisplay(guild, value);
    }

    public IGuildSettingType getSettingType() {
        return settingType;
    }
}
