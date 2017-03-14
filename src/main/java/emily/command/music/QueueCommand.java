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

import emily.command.CommandReactionListener;
import emily.command.CommandVisibility;
import emily.command.ICommandReactionListener;
import emily.command.PaginationInfo;
import emily.core.AbstractCommand;
import emily.db.model.OMusic;
import emily.handler.MusicPlayerHandler;
import emily.main.DiscordBot;
import emily.templates.Templates;
import emily.util.Emojibet;
import emily.util.Misc;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class QueueCommand extends AbstractCommand implements ICommandReactionListener<PaginationInfo> {
    private final static int ITEMS_PER_PAGE = 10;

    @Override
    public String getDescription() {
        return "check whats in the music queue";
    }

    @Override
    public String getCommand() {
        return "queue";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "queue        //overview"
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{"q"};
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        final Guild guild = ((TextChannel) channel).getGuild();
        MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
        if (args.length == 0) {
            if (player.getQueue().isEmpty()) {
                return Templates.music.queue_is_empty.formatGuild(guild.getId(), guild);
            }
            int maxPage = (int) Math.ceil((double) player.getQueue().size() / (double) ITEMS_PER_PAGE);
            channel.sendMessage(printQueue(guild, player.getQueue(), 1, maxPage)).queue(
                    message -> {
                        if (maxPage > 1) {
                            bot.commandReactionHandler.addReactionListener(
                                    guild.getId(), message,
                                    getReactionListener(author.getId(), new PaginationInfo(1, maxPage, guild)));
                        }
                    }
            );
            return "";
        }
        return "";
    }

    private MessageEmbed printQueue(Guild guild, List<OMusic> queue, int page, int maxpage) {
        EmbedBuilder eb = new EmbedBuilder();
        StringBuilder sb = new StringBuilder();
        sb.append("There are **").append(queue.size())
                .append("** tracks in the queue with an estimated playtime of **")
                .append(Misc.getDurationString(queue.stream().mapToLong(oMusic -> oMusic.duration).sum())).append("**\n\n");
        int start = Math.max(0, (page - 1) * ITEMS_PER_PAGE);
        int end = Math.min(queue.size() - 1, start + ITEMS_PER_PAGE);
        for (int i = start; i < end; i++) {
            OMusic music = queue.get(i);
            Member member = guild.getMemberById(music.requestedBy);
            sb.append("`").append(music.youtubecode).append("`")
                    .append(" | `")
                    .append(Misc.getDurationString(music.duration))
                    .append("` | ")
                    .append(music.youtubeTitle.substring(0, Math.min(50, music.youtubeTitle.length())));
            if (member != null) {
                sb.append(" | ").append(member.getEffectiveName());
            }
            sb.append("\n");
        }
        eb.setFooter(String.format("Page [%s / %s]", page, maxpage), null);
        return eb.setDescription(sb.toString()).build();
    }

    @Override
    public CommandReactionListener<PaginationInfo> getReactionListener(String userId, PaginationInfo initialData) {
        CommandReactionListener<PaginationInfo> listener = new CommandReactionListener<>(userId, initialData);
        listener.setExpiresIn(TimeUnit.MINUTES, 2);
        listener.registerReaction(Emojibet.PREV_TRACK, o -> {
            if (listener.getData().previousPage()) {
                o.editMessage(printQueue(initialData.getGuild(), MusicPlayerHandler.getFor(o.getGuild()).getQueue(), listener.getData().getCurrentPage(), listener.getData().getMaxPage())).queue();
            }
        });
        listener.registerReaction(Emojibet.NEXT_TRACK, o -> {
            if (listener.getData().nextPage()) {
                o.editMessage(printQueue(initialData.getGuild(), MusicPlayerHandler.getFor(o.getGuild()).getQueue(), listener.getData().getCurrentPage(), listener.getData().getMaxPage())).queue();
            }
        });
        return listener;
    }
}
