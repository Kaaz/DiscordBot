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

package emily.command.music;

import emily.command.CommandVisibility;
import emily.core.AbstractCommand;
import emily.handler.Template;
import emily.main.DiscordBot;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * Stream from url
 */
public class StreamCommand extends AbstractCommand {
    public StreamCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "Use a stream as input for the music source";
    }

    @Override
    public boolean isListed() {
        return false;
    }

    @Override
    public String getCommand() {
        return "stream";
    }

    @Override
    public String[] getUsage() {
        return new String[]{};
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
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        TextChannel tc = (TextChannel) channel;
        if (!tc.getGuild().getAudioManager().isConnected()) {
            return Template.get("music_no_users_in_channel");
        }
        bot.addStreamToQueue(args[0], tc.getGuild());
        return Template.get("music_streaming_from_url");
    }
}