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

package emily.core;

import emily.command.CommandCategory;
import emily.command.CommandVisibility;
import emily.main.DiscordBot;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public abstract class AbstractCommand {

    private CommandCategory commandCategory = CommandCategory.UNKNOWN;

    public AbstractCommand() {

    }

    /**
     * A short discription of the method
     *
     * @return description
     */
    public abstract String getDescription();

    /**
     * What should be typed to trigger this command (Without prefix)
     *
     * @return command
     */
    public abstract String getCommand();

    /**
     * How to use the command?
     *
     * @return command usage
     */
    public abstract String[] getUsage();

    /**
     * aliases to call the command
     *
     * @return array of aliases
     */
    public abstract String[] getAliases();

    public CommandCategory getCommandCategory() {
        return commandCategory;
    }

    /**
     * The command will be set to the category matching the last part of the package name.
     *
     * @param newCategory category of the command
     */
    public void setCommandCategory(CommandCategory newCategory) {
        commandCategory = newCategory;
    }

    /**
     * where can the command be used?
     *
     * @return private, public, both
     */
    public CommandVisibility getVisibility() {
        return CommandVisibility.BOTH;
    }

    /**
     * is a command enabled? it is by default
     * This enables/disables commands on a global scale
     *
     * @return command is enabled?
     */
    public boolean isEnabled() {
        return true;
    }

    /**
     * Whether the command can be blacklisted by guilds
     *
     * @return can be blacklisted?
     */
    public boolean canBeDisabled() {
        return true;
    }

    /**
     * Is a command listed? it is by default
     *
     * @return shows up in the !help list?
     */
    public boolean isListed() {
        return true;
    }

    /**
     * @param bot     the shard where its executing on
     * @param args    arguments for the command
     * @param channel channel where the command is executed
     * @param author  who invoked the command
     * @return the message to output or an empty string for nothing
     */
    public abstract String execute(DiscordBot bot, String[] args, MessageChannel channel, User author);
}
