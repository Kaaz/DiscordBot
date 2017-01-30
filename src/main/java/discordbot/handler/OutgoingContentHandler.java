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

package discordbot.handler;

import discordbot.handler.discord.RoleModifyTask;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class OutgoingContentHandler {
    private final DiscordBot botInstance;
    private final RoleModifier roleThread;

    public OutgoingContentHandler(DiscordBot b) {
        botInstance = b;
        roleThread = new RoleModifier();
    }

    /**
     * @param channel  channel to send to
     * @param content  the message
     * @param callback callback to execute after the message is sent
     */
    public void sendAsyncMessage(MessageChannel channel, String content, Consumer<Message> callback) {
        if (channel == null || content == null || content.isEmpty()) {
            return;
        }
        if (callback == null) {
            sendAsyncMessage(channel, content);
            return;
        }
        channel.sendMessage(content.substring(0, Math.min(1999, content.length()))).queue(callback,
                throwable -> {
                    Launcher.logToDiscord(throwable, "channel", channel.getId(), "content", content);
                    callback.accept(null);
                });
    }

    public void sendAsyncMessage(MessageChannel channel, String content) {
        channel.sendMessage(content.substring(0, Math.min(1999, content.length()))).queue((message) -> {
            if (botInstance.shouldCleanUpMessages(channel)) {
                botInstance.schedule(() -> saveDelete(message), Config.DELETE_MESSAGES_AFTER, TimeUnit.MILLISECONDS);
            }
        }, throwable -> Launcher.logToDiscord(throwable, "channel", channel.getId(), "content", content));
    }

    public void editAsync(Message message, String content) {
        message.editMessage(content.substring(0, Math.min(1999, content.length()))).queue();
    }

    /**
     * adds a role to a user
     *
     * @param user the user
     * @param role the role
     */
    public void addRole(User user, Role role) {
        roleThread.offer(new RoleModifyTask(user, role, true));
    }

    /**
     * removes a role from a user
     *
     * @param user the user
     * @param role the role
     */
    public void removeRole(User user, Role role) {
        roleThread.offer(new RoleModifyTask(user, role, false));
    }

    /**
     * send a message to creator {@link Config#CREATOR_ID}
     * has to be in the {@link Config#BOT_GUILD_ID } bot's guild
     *
     * @param message the message to send
     */
    public void sendMessageToCreator(String message) {
        User user = botInstance.client.getUserById(Config.CREATOR_ID);
        if (user != null) {
            sendPrivateMessage(user, message);
        } else {
            sendPrivateMessage(botInstance.getContainer().getShardFor(Config.BOT_GUILD_ID).client.getUserById(Config.CREATOR_ID), message);
        }
    }

    /**
     * Sends a private message to user
     *
     * @param target  the user to send it to
     * @param message the message
     */
    public void sendPrivateMessage(User target, String message) {
        sendPrivateMessage(target, message, null, null);
    }

    public void sendPrivateMessage(User target, String message, final Consumer<Message> onSuccess, final Consumer<Throwable> onFailed) {
        if (target != null && !target.isFake() && message != null && !message.isEmpty()) {
            target.openPrivateChannel().queue(c -> c.sendMessage(message).queue(
                    onSuccess,
                    throwable -> {
                        Launcher.logToDiscord(throwable,
                                "user", target.getName() + "#" + target.getDiscriminator(),
                                "message", message
                        );
                        if (onFailed != null) {
                            onFailed.accept(throwable);
                        }
                    }
            ));
        }
    }

    /**
     * Retrieves the message again before deleting it
     * Mostly for delayed deletion
     *
     * @param messageToDelete the message to delete
     */
    public void saveDelete(Message messageToDelete) {
        if (messageToDelete != null) {
            TextChannel channel = messageToDelete.getJDA().getTextChannelById(messageToDelete.getChannel().getId());
            if (channel != null && PermissionUtil.checkPermission(channel, channel.getGuild().getSelfMember(), Permission.MESSAGE_HISTORY)) {
                channel.getMessageById(messageToDelete.getId()).queue(msg -> {
                    if (msg != null) {
                        msg.deleteMessage().queue();
                    }
                });
            }
        }
    }


    private class RoleModifier extends Thread {
        private LinkedBlockingQueue<RoleModifyTask> itemsToDelete = new LinkedBlockingQueue<>();
        private volatile boolean processTerminated = false;

        RoleModifier() {
            start();
        }

        public void run() {
            try {
                while (!Launcher.isBeingKilled) {
                    final RoleModifyTask roleToModify = itemsToDelete.take();
                    if (roleToModify != null) {
                        Guild guild = roleToModify.getRole().getGuild();
                        Member member = guild.getMember(roleToModify.getUser());
                        if (roleToModify.isAdd()) {
                            guild.getController().addRolesToMember(member, roleToModify.getRole()).queue();
                        } else {
                            guild.getController().removeRolesFromMember(member, roleToModify.getRole()).queue();
                        }
                    }
                    sleep(2_000L);
                }
            } catch (InterruptedException ignored) {
            } finally {
                processTerminated = true;
            }
        }

        public void offer(RoleModifyTask lm) {
            if (processTerminated) return;
            itemsToDelete.offer(lm);
        }
    }
}