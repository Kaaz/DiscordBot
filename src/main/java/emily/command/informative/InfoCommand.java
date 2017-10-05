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

import emily.command.CooldownScope;
import emily.command.ICommandCooldown;
import emily.core.AbstractCommand;
import emily.handler.CommandHandler;
import emily.main.BotConfig;
import emily.main.DiscordBot;
import emily.main.Launcher;
import emily.util.DisUtil;
import emily.util.TimeUtil;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import org.trello4j.Trello;
import org.trello4j.TrelloImpl;
import org.trello4j.model.Card;
import org.trello4j.model.Checklist;

import java.util.List;


/**
 * !info
 * some general information about the bot
 */
public class InfoCommand extends AbstractCommand implements ICommandCooldown {
    private Trello trello;

    public InfoCommand() {
        super();
        trello = new TrelloImpl(BotConfig.TRELLO_API_KEY, BotConfig.TRELLO_TOKEN);
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public long getCooldownDuration() {
        return 15L;
    }

    @Override
    public CooldownScope getScope() {
        return CooldownScope.CHANNEL;
    }

    @Override
    public String getDescription() {
        return "Shows some general information about me and my future plans.";
    }

    @Override
    public String getCommand() {
        return "info";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "info          //general info",
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "about"
        };
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        if (args.length > 0 && BotConfig.TRELLO_ACTIVE) {
            switch (args[0].toLowerCase()) {
                case "planned":
                case "plan":
                    return "The following items are planned:" + BotConfig.EOL + getListFor(BotConfig.TRELLO_LIST_PLANNED, ":date:");
                case "bugs":
                case "bug":
                    return "The following bugs are known:" + BotConfig.EOL + getListFor(BotConfig.TRELLO_LIST_BUGS, ":exclamation:");
                case "progress":
                    return "The following items are being worked on:" + BotConfig.EOL + getListFor(BotConfig.TRELLO_LIST_IN_PROGRESS, ":construction:");
                default:
                    break;
            }//
        }
        channel.sendTyping();
        String onlineFor = TimeUtil.getRelativeTime(bot.startupTimeStamp, false);
        String response = bot.chatBotHandler.chat("emily", "What are you?");
        String purpose = bot.chatBotHandler.chat("emily", "What is your purpose?");
        String prefix = DisUtil.getCommandPrefix(channel);
        if (response.isEmpty()) {
            response = "I'm batman";
        }
        if (purpose.isEmpty()) {
            purpose = "I don't know";
        }
        return "\u2139 > Info  " + BotConfig.EOL + BotConfig.EOL +
                "*" + bot.chatBotHandler.chat("emily", "information") + "* " + BotConfig.EOL + BotConfig.EOL +
                "**What am I?** *" + response + "* " + BotConfig.EOL +
                "**My purpose?** *" + purpose + "* " + BotConfig.EOL +
                "**Who made me?** *Kaaz*" + BotConfig.EOL + BotConfig.EOL +
                "The last time I restarted was  " + onlineFor + "." + BotConfig.EOL +
                "Running version `" + Launcher.getVersion().toString() + "`. You can use `" + prefix + "changelog` to see what changed." + BotConfig.EOL + BotConfig.EOL +
                "Type **" + prefix + "help** to see what I'll allow you to do. In total there are " + CommandHandler.getCommands().length + " commands I can perform." + BotConfig.EOL + BotConfig.EOL +
                "For help about a specific command type `" + prefix + "<command> help`" + BotConfig.EOL +
                "An example: `" + prefix + "skip help` to see what you can do with the skip command." + BotConfig.EOL + BotConfig.EOL +
                "If you need assistance, want to share your thoughts or want to contribute feel free to join my __" + prefix + "discord__";
    }

    private String getListFor(String listId, String itemPrefix) {
        StringBuilder sb = new StringBuilder();
        List<Card> cardsByList = trello.getCardsByList(listId);
        for (Card card : cardsByList) {
            sb.append(itemPrefix).append(" **").append(card.getName()).append("**").append(BotConfig.EOL);
            if (card.getDesc().length() > 2) {
                sb.append(card.getDesc()).append(BotConfig.EOL);
            }
            List<Checklist> checkItemStates = trello.getChecklistByCard(card.getId());
            for (Checklist clist : checkItemStates) {
                sb.append(BotConfig.EOL);
                for (Checklist.CheckItem item : clist.getCheckItems()) {
                    sb.append(String.format(" %s %s", item.isChecked() ? ":ballot_box_with_check:" : ":white_large_square:", item.getName())).append(BotConfig.EOL);
                }
            }

            sb.append(BotConfig.EOL);
        }
        if (sb.length() == 0) {
            sb.append("There are currently no items!");
        }
        return BotConfig.EOL + sb.toString();
    }
}