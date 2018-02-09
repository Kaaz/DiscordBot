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

import emily.core.AbstractCommand;
import emily.main.DiscordBot;
import emily.main.Launcher;
import emily.util.UpdateUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;


/**
 * !version
 * some general information about the bot
 */
public class VersionCommand extends AbstractCommand {

    public VersionCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "Shows what versions I'm using";
    }

    @Override
    public String getCommand() {
        return "version";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "version  //version usage"
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "v"
        };
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        return "Info about the versions:" + "\n" +
                "Current version: `" + Launcher.getVersion() + "`" + "\n" +
                "Latest  version: `" + UpdateUtil.getLatestVersion() + "`" + "\n";
    }
}