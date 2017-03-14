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

import emily.command.CommandReactionListener;
import emily.command.ICommandReactionListener;
import emily.command.PaginationInfo;
import emily.core.AbstractCommand;
import emily.db.controllers.CGuild;
import emily.db.controllers.CTodoItems;
import emily.db.controllers.CTodoLists;
import emily.db.controllers.CUser;
import emily.db.model.OTodoItem;
import emily.db.model.OTodoList;
import emily.handler.Template;
import emily.main.DiscordBot;
import emily.util.DisUtil;
import emily.util.Emojibet;
import emily.util.Misc;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;
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
                "todo list <name/code>        //check todo items of a list",
                "todo add <text>              //adds a todo item to your list",
                "todo remove <id>             //removes a todo item from your list",
                "todo check <text>            //marks an item as checked",
                "todo uncheck <text>          //marks an item as unchecked",
                "todo clearchecked            //deletes checked items",
                "todo priority <number> <priority>     //sets a priority of a todo item",
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
            return makeListFor(author, rec);
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
            case "clearchecked":
            case "deletechecked":
                if (rec.id == 0) {
                    return Template.get("todo_your_list_not_found");
                }
                CTodoItems.deleteChecked(rec.id);
                return Template.get("todo_list_cleared");
            case "user":
                if (args.length == 1) {
                    return Template.get("command_invalid_use");
                }
                User user = DisUtil.findUser((TextChannel) channel, Misc.joinStrings(args, 1));
                if (user == null) {
                    return Template.get("cant_find_user", Misc.joinStrings(args, 1));
                }
                OTodoList userList = CTodoLists.findBy(CUser.getCachedId(user.getId()));
                if (userList.id == 0) {
                    return Template.get("todo_user_list_not_found", user.getName());
                }
                return makeListFor(user, rec);
        }
        if (rec.id == 0 || args.length < 2) {
            return Template.get("command_invalid_use");
        }
        switch (args[0].toLowerCase()) {
            case "add":
                OTodoItem item = new OTodoItem();
                item.listId = rec.id;
                item.description = Misc.joinStrings(args, 1);
                CTodoItems.insert(item);
                return Template.get("todo_item_add_success");
            case "remove":
                OTodoItem editItem = CTodoItems.findBy(Misc.parseInt(args[1], 0));
                if (editItem.listId != rec.id) {
                    return Template.get("todo_not_your_item");
                }
                CTodoItems.delete(editItem);
                return Template.get("todo_item_removed");
            case "uncheck":
            case "check":
                OTodoItem check = CTodoItems.findBy(Misc.parseInt(args[1], 0));
                if (check.listId != rec.id || check.id == 0) {
                    return Template.get("todo_not_your_item");
                }
                check.checked = args[0].equals("check") ? 1 : 0;
                CTodoItems.update(check);
                return Template.get("todo_item_updated");
            case "priority":
                if (args.length < 3) {
                    return Template.get("command_invalid_use");
                }
                OTodoItem priority = CTodoItems.findBy(Misc.parseInt(args[1], 0));
                if (priority.listId != rec.id || priority.id == 0) {
                    return Template.get("todo_not_your_item");
                }
                priority.priority = Misc.parseInt(args[2], 0);
                CTodoItems.update(priority);
                return Template.get("todo_item_updated");
        }
        return Emojibet.EYES;
    }

    private String makeListFor(User user, OTodoList rec) {

        List<OTodoItem> list = CTodoItems.getListFor(rec.id);
        if (list.isEmpty()) {
            return "The todo list is empty!";
        }
        String out = "Todo list for " + user.getName() + ": \n\n" + Emojibet.NOTEPAD + " " + rec.listName + " \n\n";
        for (OTodoItem item : list) {
            out += String.format("%s`\u200B%5d` %s %s\n",
                    item.checked == 1 ? Emojibet.CHECK_MARK_GREEN : Emojibet.CHECK_BOX_UNCHECKED,
                    item.id,
                    Emojibet.HASH,
                    item.description
            );
        }
        return out;
    }

    private String makePageFor(String userId, int page) {
        return "";
    }

    @Override
    public CommandReactionListener<PaginationInfo<String>> getReactionListener(String userId, PaginationInfo<String> initialData) {
        CommandReactionListener<PaginationInfo<String>> listener = new CommandReactionListener<>(userId, initialData);
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
