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

import emily.command.meta.CommandVisibility;
import emily.command.meta.AbstractCommand;
import emily.db.controllers.CChannels;
import emily.db.controllers.CGuild;
import emily.db.controllers.CServices;
import emily.db.controllers.CSubscriptions;
import emily.db.model.OService;
import emily.db.model.OSubscription;
import emily.db.model.QActiveSubscriptions;
import emily.main.DiscordBot;
import emily.templates.Templates;
import emily.util.Misc;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * !subscribe
 * subscripe to certain events
 */
public class Subscribe extends AbstractCommand {
    public Subscribe() {
        super();
    }

    @Override
    public String getDescription() {
        return "subscribe the channel to certain events";
    }

    @Override
    public String getCommand() {
        return "subscribe";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "subscribe                //check what subscriptions are active",
                "subscribe <name>         //subscribe to subject",
                "subscribe stop <name>    //stop subscription to subject",
//                "subscribe info <name>    //information about subject",
                "subscribe list           //See what subscription options there are",
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "sub"
        };
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        TextChannel txt = (TextChannel) channel;
        List<String> headers = new ArrayList<>();
        List<List<String>> tbl = new ArrayList<>();
        if (args.length == 0) {
            Collections.addAll(headers, "code", "name");
            List<QActiveSubscriptions> subscriptionsForChannel = CSubscriptions.getSubscriptionsForChannel(CChannels.getCachedId(txt.getIdLong(), txt.getGuild().getIdLong()));
            for (QActiveSubscriptions subscriptions : subscriptionsForChannel) {
                ArrayList<String> row = new ArrayList<>();
                row.add(subscriptions.code);
                row.add(subscriptions.displayName);
                tbl.add(row);
            }
            if (tbl.size() > 0) {
                return "Active Subscriptions" + "\n" +
                        "This channel is currenty subscribed for: " +
                        Misc.makeAsciiTable(headers, tbl, null);
            }
            return Templates.command.subscribe.channel_has_no_subscriptions.formatGuild(channel) + "\n" +
                    "Possible options to subscribe to: " +
                    getServicesTable();
        }
        if (args[0].equalsIgnoreCase("stop")) {
            if (args.length > 1) {
                OService service = CServices.findBy(args[1].trim());
                if (service.id == 0) {
                    return Templates.command.subscribe.invalid_service.formatGuild(channel);
                }
                OSubscription subscription = CSubscriptions.findBy(CGuild.getCachedId(txt.getGuild().getIdLong()), CChannels.getCachedId(channel.getIdLong(), txt.getGuild().getIdLong()), service.id);
                if (subscription.subscribed == 1) {
                    subscription.subscribed = 0;
                    CSubscriptions.insertOrUpdate(subscription);
                    return Templates.command.subscribe.unsubscribed_success.formatGuild(channel, service.displayName);
                }
                return Templates.command.subscribe.not_subscribed.formatGuild(channel);
            }
            return Templates.invalid_use.formatGuild(channel);
        } else if (args[0].equalsIgnoreCase("info")) {
            if (args.length > 1) {
                Templates.not_implemented_yet.formatGuild(channel); //@todo <--
            }
            return Templates.invalid_use.formatGuild(channel);
        } else if (args[0].equalsIgnoreCase("list")) {
            return "Subscriptions" + "\n" +
                    "Possible options to subscribe to: " +
                    getServicesTable();
        }
        OService service = CServices.findBy(args[0].trim());
        if (service.id == 0) {
            return Templates.command.subscribe.invalid_service.formatGuild(channel);
        }
        OSubscription subscription = CSubscriptions.findBy(CGuild.getCachedId(txt.getGuild().getIdLong()), CChannels.getCachedId(channel.getIdLong(), ((TextChannel) channel).getGuild().getIdLong()), service.id);
        if (subscription.subscribed == 0) {
            subscription.subscribed = 1;
            subscription.channelId = CChannels.getCachedId(channel.getIdLong(), txt.getGuild().getIdLong());
            subscription.serverId = CGuild.getCachedId(txt.getGuild().getIdLong());
            subscription.serviceId = service.id;
            CSubscriptions.insertOrUpdate(subscription);
            return Templates.command.subscribe.success.formatGuild(channel);
        }
        return Templates.command.subscribe.already_subscribed.formatGuild(channel);
    }

    private String getServicesTable() {
        List<List<String>> table = new ArrayList<>();
        List<OService> allActive = CServices.getAllActive();
        for (OService service : allActive) {
            ArrayList<String> row = new ArrayList<>();
            row.add(service.name);
            row.add(service.displayName);
            table.add(row);
        }
        return Misc.makeAsciiTable(Arrays.asList("code", "name"), table, null);
    }
}