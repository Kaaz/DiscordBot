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

package emily.command.poe;

import com.google.common.base.Joiner;
import emily.core.AbstractCommand;
import emily.main.DiscordBot;
import emily.modules.pathofexile.ItemAnalyzer;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !poeitem
 * Analyzes an item from path of exile
 */
public class PoeItemCommand extends AbstractCommand {
    public PoeItemCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "Analyzes an item from path of exile.";
    }

    @Override
    public String getCommand() {
        return "poeitem";
    }

    @Override
    public String[] getUsage() {
        return new String[]{};
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        String input = Joiner.on(" ").join(args);
        ItemAnalyzer itemAnalyzer = new ItemAnalyzer();
        return itemAnalyzer.attemptToANALyze(input).toString();

//		return TextHandler.get("command_not_implemented");
    }
}