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

package emily.command.administrative;

import emily.command.meta.AbstractCommand;
import emily.command.meta.CommandVisibility;
import emily.main.DiscordBot;
import emily.templates.Templates;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !role
 * manages roles
 */
public class SetupCommand extends AbstractCommand {
    public SetupCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "Guided setup of basic configuration settings.\n";
    }

    @Override
    public String getCommand() {
        return "setup";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "setup guild              //guided setup of guild-specific settings.",
                "setup bot                //guided setup of core bot settings."
        };
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        if(args.length == 1){
            if(args[0].equalsIgnoreCase("guild")){
                channel.sendMessage("guild response").queue();
            }else if(args[0].equalsIgnoreCase("bot")){
                channel.sendMessage("bot response").queue();
            }else{
                return Templates.invalid_use.formatGuild(channel);
            }
            return "Command success";
        }
        return Templates.invalid_use.formatGuild(channel);
    }
}