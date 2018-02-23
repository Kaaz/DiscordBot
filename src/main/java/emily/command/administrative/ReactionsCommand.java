/*
 * Copyright 2018 github.com/kaaz
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

import emily.command.reactions.ReactionType;
import emily.command.reactions.Reactions;
import emily.core.AbstractCommand;
import emily.main.DiscordBot;
import emily.util.Emojibet;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class ReactionsCommand extends AbstractCommand {
    @Override
    public String getDescription() {
        return "Configure what reactions do to messages";
    }

    @Override
    public String getCommand() {
        return "reactions";
    }

    @Override
    public boolean isListed() {
        return false;
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "reactions       //settings for user placed reactions ",
                "reactions music //reactions for now playing message"
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{"reaction", "r"};
    }

    private String buildMessage() {
        String[] onoff = {Emojibet.OKE_SIGN, Emojibet.X};
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (ReactionType reactionType : ReactionType.values()) {
            sb.append("**").append(reactionType.getTitle()).append("** \n");
            sb.append(reactionType.getDescription()).append("\n\n");
            for (Reactions reactions : reactionType.getReactions()) {
                sb.append(onoff[++i % 2]).append(" | ").append(reactions.getEmote()).append(" | ").append(reactions.getDescription()).append("\n");
            }
            sb.append("\n\n");
        }
        return sb.toString();
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        return "The following :\n" + buildMessage();
    }
}
