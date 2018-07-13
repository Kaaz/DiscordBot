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

package emily.command.informative;

import emily.command.meta.AbstractCommand;
import emily.main.DiscordBot;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !invite
 * Instructions on how to invite the bot to a discord server
 */
public class InviteCommand extends AbstractCommand {
    public InviteCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "Provides an invite link to add the bot to your server.";
    }

    @Override
    public String getCommand() {
        return "invite";
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public String[] getUsage() {
        return new String[]{};
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "inv"
        };
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        return "I am honored you'd want to invite me! :hugging:\n" +
                "You can add me to your guild/server with the following link:\n" +
                "https://discordapp.com/oauth2/authorize?client_id=" + channel.getJDA().getSelfUser().getId() + "&scope=bot&permissions=339209287";
    }
}