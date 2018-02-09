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

package emily.command.bot_administration;

import emily.core.AbstractCommand;
import emily.handler.Template;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.util.Emojibet;
import emily.util.Misc;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !botstatus
 * changes the bot status (the playing game, or streaming)
 */
public class BotStatusCommand extends AbstractCommand {
    public BotStatusCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "Set the game I'm currently playing";
    }

    @Override
    public String getCommand() {
        return "botstatus";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "botstatus reset                      //unlocks the status",
                "botstatus game <game>                //changes the playing game to <game>",
                "botstatus stream <username> <game>   //streaming twitch.tv/<username> playing <game>",
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        SimpleRank rank = bot.security.getSimpleRank(author);
        if (!rank.isAtLeast(SimpleRank.BOT_ADMIN)) {
            return Template.get(channel, "command_no_permission");
        }
        if (args.length == 0) {
            return Template.get("command_invalid_use");
        }
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "reset":
                    bot.getContainer().setStatusLocked(false);
                    return Emojibet.THUMBS_UP;
                case "game":
                    if (args.length < 2) {
                        return Template.get("command_invalid_use");
                    }
                    channel.getJDA().getPresence().setGame(Game.of(Game.GameType.DEFAULT, Misc.joinStrings(args, 1)));
                    break;
                case "stream":
                    if (args.length < 3) {
                        return Template.get("command_invalid_use");
                    }
                    try {
                        channel.getJDA().getPresence().setGame(Game.of(Game.GameType.DEFAULT, Misc.joinStrings(args, 2), "http://www.twitch.tv/" + args[1]));
                    } catch (Exception e) {
                        return Emojibet.THUMBS_DOWN + " " + e.getMessage();
                    }
                    break;
                default:
                    return Template.get("command_invalid_use");
            }
            bot.getContainer().setStatusLocked(true);
            try {
                Thread.sleep(5_000L);
            } catch (InterruptedException ignored) {
            }
            return Emojibet.THUMBS_UP;
        }
        return Template.get("command_invalid_use");
    }
}