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
import emily.db.controllers.CGuild;
import emily.db.controllers.CTodoItems;
import emily.db.controllers.CTodoLists;
import emily.db.controllers.CUser;
import emily.db.model.OTodoItem;
import emily.db.model.OTodoList;
import emily.main.DiscordBot;
import emily.templates.Templates;
import emily.util.DisUtil;
import emily.util.Emojibet;
import emily.util.Misc;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;

public class ToDoCommand extends AbstractCommand {

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
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        OTodoList rec = CTodoLists.findBy(CUser.getCachedId(author.getIdLong()));
        if (args.length == 0) {
            if (rec.id == 0) {
                return Templates.todo.your_list_not_found.formatGuild(channel);
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
                    rec.userId = CUser.getCachedId(author.getIdLong());
                }
                CTodoLists.update(rec);
                return Templates.todo.list_updated.formatGuild(channel);
            case "clearchecked":
            case "deletechecked":
                if (rec.id == 0) {
                    return Templates.todo.your_list_not_found.formatGuild(channel);
                }
                CTodoItems.deleteChecked(rec.id);
                return Templates.todo.list_cleared.formatGuild(channel);
            case "user":
                if (args.length == 1) {
                    return Templates.invalid_use.formatGuild(channel);
                }
                User user = DisUtil.findUser((TextChannel) channel, Misc.joinStrings(args, 1));
                if (user == null) {
                    return Templates.config.cant_find_user.formatGuild(channel, Misc.joinStrings(args, 1));
                }
                OTodoList userList = CTodoLists.findBy(CUser.getCachedId(user.getIdLong()));
                if (userList.id == 0) {
                    return Templates.todo.user_list_not_found.formatGuild(channel, user);
                }
                return makeListFor(user, rec);
        }
        if (rec.id == 0 || args.length < 2) {
            return Templates.invalid_use.formatGuild(channel);
        }
        switch (args[0].toLowerCase()) {
            case "add":
                OTodoItem item = new OTodoItem();
                item.listId = rec.id;
                item.description = Misc.joinStrings(args, 1);
                CTodoItems.insert(item);
                return Templates.todo.item_add_success.formatGuild(channel);
            case "remove":
                OTodoItem editItem = CTodoItems.findBy(Misc.parseInt(args[1], 0));
                if (editItem.listId != rec.id) {
                    return Templates.todo.not_your_item.formatGuild(channel);
                }
                CTodoItems.delete(editItem);
                return Templates.todo.item_removed.formatGuild(channel);
            case "uncheck":
            case "check":
                OTodoItem check = CTodoItems.findBy(Misc.parseInt(args[1], 0));
                if (check.listId != rec.id || check.id == 0) {
                    return Templates.todo.not_your_item.formatGuild(channel);
                }
                check.checked = args[0].equals("check") ? 1 : 0;
                CTodoItems.update(check);
                return Templates.todo.item_updated.formatGuild(channel);
            case "priority":
                if (args.length < 3) {
                    return Templates.invalid_use.formatGuild(channel);
                }
                OTodoItem priority = CTodoItems.findBy(Misc.parseInt(args[1], 0));
                if (priority.listId != rec.id || priority.id == 0) {
                    return Templates.todo.not_your_item.formatGuild(channel);
                }
                priority.priority = Misc.parseInt(args[2], 0);
                CTodoItems.update(priority);
                return Templates.todo.item_updated.formatGuild(channel);
        }
        return Emojibet.EYES;
    }

    private String makeListFor(User user, OTodoList rec) {

        List<OTodoItem> list = CTodoItems.getListFor(rec.id);
        if (list.isEmpty()) {
            return "The todo list is empty!";
        }
        StringBuilder out = new StringBuilder("Todo list for " + user.getName() + ": \n\n" + Emojibet.NOTEPAD + " " + rec.listName + " \n\n");
        for (OTodoItem item : list) {
            out.append(String.format("%s`\u200B%5d` %s %s\n",
                    item.checked == 1 ? Emojibet.CHECK_MARK_GREEN : Emojibet.CHECK_BOX_UNCHECKED,
                    item.id,
                    Emojibet.HASH,
                    item.description
            ));
        }
        return out.toString();
    }

    private String makePageFor(String userId, int page) {
        return "";
    }
}
