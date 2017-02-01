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

package discordbot.command.informative;

import discordbot.core.AbstractCommand;
import discordbot.main.DiscordBot;
import discordbot.util.Emojibet;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class ToDoCommand extends AbstractCommand {
    @Override
    public String getDescription() {
        return "administer todo items";
    }

    @Override
    public String getCommand() {
        return "todo";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "todo                                  //overview of your items",
                "todo user <name>                      //check todo items of user ",
                "todo add <text>                       //adds a todo item to your list",
                "todo check <number>                   //marks an item as checked",
                "todo priority <number> <priority>     //sets a priority of a todo item",
                "todo tags <number> <tags>             //todo"
        };
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        return Emojibet.EYES;
    }
}
