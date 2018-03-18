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

package emily.command.fun;

import emily.command.meta.CommandVisibility;
import emily.command.meta.AbstractCommand;
import emily.main.DiscordBot;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * This is just a dummy class so it shows up in the !help function
 * Game is actually the {@link emily.handler.GameHandler}
 */
public class GameCommandCommand extends AbstractCommand {

    public GameCommandCommand() {
        super();
    }

    @Override
    public boolean isListed() {
        return true;
    }

    @Override
    public String getDescription() {
        return "play games against eachother!";
    }

    @Override
    public String getCommand() {
        return "game";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "game list                 //to see a list games",
                "game <@user> <gamecode>   //play a game against @user",
                "game cancel               //cancel an active game!"

        };
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        return "";//for the implementation see GameHandler
    }
}