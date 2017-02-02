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

import discordbot.command.CommandReactionListener;
import discordbot.command.ICommandReactionListener;
import discordbot.command.PaginationInfo;
import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CGuild;
import discordbot.db.controllers.CTodoItems;
import discordbot.db.controllers.CTodoLists;
import discordbot.db.controllers.CUser;
import discordbot.db.model.OTodoItem;
import discordbot.db.model.OTodoList;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.util.Emojibet;
import discordbot.util.Misc;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.concurrent.TimeUnit;

public class ToDoCommand extends AbstractCommand implements ICommandReactionListener<PaginationInfo<String>> {
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
                "todo                         //overview of your lists items",
                "todo create                  //creates the list",
                "todo listname <name>         //sets the name",
                "todo list <name/code>        //check todo items of a list",
                "todo add <text>              //adds a todo item to your list",
                "todo add <list> <text>       //adds a todo item to a list",
                "todo remove <id>             //removes a todo item from your list",
                "todo remove <list> <text>    //removes a todo item from a list",
                "todo check <text>            //marks an item as checked",
                "todo check <list> <text>     //marks an item as checked",
                "todo priority <list><number> <priority>     //sets a priority of a todo item",
        };
    }
//another use case might be; I'd want invoices to be numbered per month, and every month I want it to start on 1, so to do this I'd like the invoiceNumber to be an inc
    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        OTodoList rec = CTodoLists.findBy(CUser.getCachedId(author.getId()));
        if (args.length == 0) {
            if (rec.id == 0) {
                return Template.get("todo_your_list_not_found");
            }
            return "Your list: \n\n" +
                    Emojibet.NOTEPAD + " " + rec.listName + " \n";
        }
        switch (args[0].toLowerCase()) {
            case "create":
            case "listname":
                String name;
                if (args.length > 1) {
                    name = Misc.joinStrings(args, 1);
                } else {
                    name = author.getName() + "'s todo list";
                }
                rec.listName = name;
                if (rec.id == 0) {
                    rec.guildId = CGuild.getCachedId(channel);
                    rec.userId = CUser.getCachedId(author.getId());
                }
                CTodoLists.update(rec);
                return Template.get("todo_list_updated");
            case "user":
                return "overview of user";
            case "add":
                if (rec.id > 0 && args.length > 1) {
                    OTodoItem item = new OTodoItem();
                    item.listId = rec.id;
                    item.description = Misc.joinStrings(args, 1);
                    CTodoItems.insert(item);
                    return Template.get("todo_item_add_success");
                }
                return Template.get("todo_item_add_failed");
            case "remove":
                return "remove an item";
            case "check":
                return "check or uncheck";
            case "priority":
                return "change the priority";
            case "tag":
            case "tags":
                return "add tags to items";
        }
        return Emojibet.EYES;
    }

    private String makePageFor(String userId, int page) {
        return "";
    }

    @Override
    public CommandReactionListener<PaginationInfo<String>> getReactionListener(String InvokerUserId, PaginationInfo<String> initialData) {
        CommandReactionListener<PaginationInfo<String>> listener = new CommandReactionListener<>(InvokerUserId, initialData);
        listener.setExpiresIn(TimeUnit.MINUTES, 2);
        listener.registerReaction(Emojibet.PREV_TRACK, o -> {
            if (listener.getData().previousPage()) {
//                o.editMessage(new MessageBuilder().setEmbed(makeEmbedConfig(data.getGuild(), listener.getData().getCurrentPage())).build()).queue();
            }
        });
        listener.registerReaction(Emojibet.NEXT_TRACK, o -> {
            if (listener.getData().nextPage()) {
//                o.editMessage(new MessageBuilder().setEmbed(makeEmbedConfig(data.getGuild(), listener.getData().getCurrentPage())).build()).queue();
            }
        });
        return listener;
    }
}
