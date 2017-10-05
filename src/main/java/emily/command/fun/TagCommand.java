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

package emily.command.fun;

import com.vdurmont.emoji.EmojiParser;
import emily.command.CommandReactionListener;
import emily.command.CommandVisibility;
import emily.command.ICommandReactionListener;
import emily.command.PaginationInfo;
import emily.core.AbstractCommand;
import emily.db.controllers.CGuild;
import emily.db.controllers.CTag;
import emily.db.controllers.CUser;
import emily.db.model.OTag;
import emily.handler.Template;
import emily.main.BotConfig;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.util.DisUtil;
import emily.util.Emojibet;
import emily.util.Misc;
import emily.util.TimeUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * !tag
 */
public class TagCommand extends AbstractCommand implements ICommandReactionListener<PaginationInfo> {
    private final int TAGS_PER_PAGE = 25;

    public TagCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "Tags!";
    }

    @Override
    public String getCommand() {
        return "tag";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "tag                     //list of tags",
                "tag <name>              //shows the tag",
                "tag mine                //shows your tags",
                "tag by <name>           //shows tags created by user",
                "tag details <tag>       //shows info about tag",
                "tag list                //shows all tags ",
                "tag deleteuser <@user>  //deletes tags by user",
                "tag delete <name>       //deletes tag",
                "tag <name> <content>    //creates the tag",
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "t", "tags"
        };
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        Guild guild = ((TextChannel) channel).getGuild();
        SimpleRank rank = bot.security.getSimpleRank(author, channel);
        if (args.length == 0 || args[0].equals("list")) {
            List<OTag> tags = CTag.getTagsFor(guild.getId());
            if (tags.isEmpty()) {
                return Template.get("command_tag_no_tags");
            }
            int tagCount = CTag.countTagsOn(CGuild.getCachedId(guild.getId()));
            if (tagCount <= TAGS_PER_PAGE) {
                return "The following tags exist: " + BotConfig.EOL + Misc.makeTable(tags.stream().map(sc -> sc.tagname).collect(Collectors.toList()));
            }
            int maxPage = (int) Math.ceil((double) CTag.countTagsOn(CGuild.getCachedId(guild.getId())) / (double) TAGS_PER_PAGE);
            bot.queue.add(channel.sendMessage(makePage(guild, 1, maxPage)),
                    message ->
                            bot.commandReactionHandler.addReactionListener(
                                    guild.getId(), message,
                                    getReactionListener(author.getId(), new PaginationInfo(1, maxPage, guild))));
            return "";

        } else if (args[0].equalsIgnoreCase("mine")) {
            List<OTag> tags = CTag.getTagsFor(guild.getId(), author.getId());
            if (tags.isEmpty()) {
                return Template.get("command_tag_no_tags");
            }
            return "You have made the following tags: " + BotConfig.EOL + Misc.makeTable(tags.stream().map(sc -> sc.tagname).collect(Collectors.toList()));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("details")) {
            OTag tag = CTag.findBy(CGuild.getCachedId(guild.getId()), args[1]);
            if (tag.id == 0) {
                return Template.get("command_tag_not_set");
            }
            User user = channel.getJDA().getUserById(CUser.getCachedDiscordId(tag.userId));
            String username = "???";
            if (user != null) {
                username = user.getName();
            }
            return String.format("The tag `%s` is created on %s by %s", tag.tagname, TimeUtil.formatYMD(tag.created), username);

        }
        if (args.length == 2 && args[0].equalsIgnoreCase("by")) {
            User user = DisUtil.findUser((TextChannel) channel, args[1]);
            if (user == null) {
                return Template.get("cant_find_user", args[1]);
            }
            List<OTag> tags = CTag.findByUser(CGuild.getCachedId(guild.getId()), CUser.getCachedId(user.getId()));
            return user.getName() + " made the following tags: " + BotConfig.EOL + Misc.makeTable(tags.stream().map(sc -> sc.tagname).collect(Collectors.toList()));
        }
        if (args.length > 1 && args[0].equalsIgnoreCase("deleteuser")) {
            if (!rank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
                return Template.get("command_no_permission");
            }
            User user = DisUtil.findUser((TextChannel) channel, args[1]);
            if (user == null) {
                return Template.get("cant_find_user", args[1]);
            }
            CTag.deleteTagsBy(CGuild.getCachedId(guild.getId()), CUser.getCachedId(user.getId()));
            return Template.get("command_tag_by_user_deleted", user.getName());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
            OTag tag = CTag.findBy(guild.getId(), args[1]);
            if (tag.id > 0) {
                if (!rank.isAtLeast(SimpleRank.GUILD_ADMIN) && CUser.getCachedId(author.getId()) != tag.userId) {
                    return Template.get("command_tag_only_delete_own");
                }
                CTag.delete(tag);
                return Template.get("command_tag_delete_success");
            }
            return Template.get("command_tag_nothing_to_delete");
        }
        if (DisUtil.hasMention(args[0])) {
            return Template.get("command_tag_no_mention");
        }
        OTag tag = CTag.findBy(guild.getId(), args[0]);
        if (args.length > 1) {
            if (tag.id > 0 && tag.userId != CUser.getCachedId(author.getId())) {
                return Template.get("command_tag_only_creator_can_edit");
            }
            String output = Misc.joinStrings(args, 1);
            output = output.trim();
            if (tag.id == 0) {
                tag.tagname = args[0].replace(BotConfig.EOL, "").trim();
                tag.guildId = CGuild.getCachedId(guild.getId());
                tag.userId = CUser.getCachedId(author.getId(), author.getName());
                tag.created = new Timestamp(System.currentTimeMillis());
            }
            tag.response = EmojiParser.parseToAliases(output);
            if (tag.response.length() > 2000) {
                tag.response = tag.response.substring(0, 1999);
            }
            CTag.insert(tag);
            return Template.get("command_tag_saved");
        }
        if (tag.id > 0) {
            return tag.response;

        }
        return Template.get("command_tag_not_set");
    }

    private String makePage(Guild guild, int activePage, int maxPage) {
        int offset = (activePage - 1) * TAGS_PER_PAGE;
        List<OTag> tags = CTag.getTagsFor(guild.getId(), offset, TAGS_PER_PAGE);
        return String.format("The following tags exist: [page %2d/%2d] ", activePage, maxPage) +
                BotConfig.EOL + Misc.makeTable(tags.stream().map(sc -> sc.tagname).collect(Collectors.toList()));
    }

    @Override
    public CommandReactionListener<PaginationInfo> getReactionListener(String userId, PaginationInfo initialData) {
        CommandReactionListener<PaginationInfo> listener = new CommandReactionListener<>(userId, initialData);
        listener.setExpiresIn(TimeUnit.MINUTES, 2);
        listener.registerReaction(Emojibet.PREV_TRACK, o -> {
            if (listener.getData().previousPage()) {
                o.editMessage(makePage(o.getGuild(), listener.getData().getCurrentPage(), listener.getData().getMaxPage())).complete();
            }
        });
        listener.registerReaction(Emojibet.NEXT_TRACK, o -> {
            if (listener.getData().nextPage()) {
                o.editMessage(makePage(o.getGuild(), listener.getData().getCurrentPage(), listener.getData().getMaxPage())).complete();
            }
        });
        return listener;
    }
}