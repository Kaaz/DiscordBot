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

package emily.handler;

import emily.handler.discord.RoleModifyTask;
import emily.main.Config;
import emily.main.DiscordBot;
import emily.main.Launcher;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class OutgoingContentHandler {
    private final DiscordBot botInstance;
    private final RoleModifier roleThread;
    private final long TIMEOUT = 10L;

    public OutgoingContentHandler(DiscordBot b) {
        botInstance = b;
        roleThread = new RoleModifier();
    }

    public Message sendBlock(MessageChannel channel, String msg) {
        if (channel == null || msg == null) {
            return null;
        }
        if (msg.length() > 2000) {
            msg = msg.substring(0, 1999);
        }
        switch (channel.getType()) {
            case PRIVATE:
                break;
            case TEXT:
                return sendToText((TextChannel) channel, msg);
            default:
                break;
        }
        return null;
    }

    public void editBlocking(Message msg, String newContent) {
        if (!msg.getChannelType().equals(ChannelType.TEXT)) {
            return;
        }
        TextChannel channel = botInstance.getJda().getTextChannelById(msg.getTextChannel().getId());
        if (channel == null) {
            return;
        }
        botInstance.queue.add(channel.editMessageById(msg.getId(), newContent));
    }

    /**
     * @param channel the channel to send it to
     * @param message the message
     */
    private Message sendToText(TextChannel channel, String message) {
        if (!channel.canTalk() || botInstance.getJda().getGuildById(channel.getGuild().getId()) == null) {
            return null;
        }
        Future<Message> future = channel.sendMessage(message).submit(true);
        try {
            return future.get(30, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            future.cancel(true);
            e.printStackTrace();
        }
        return null;
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
        botInstance.queue.add(channel.sendMessage(content.substring(0, Math.min(1999, content.length()))), callback);
    }

    public void sendAsyncMessage(MessageChannel channel, String content) {
        RestAction<Message> messageRestAction = channel.sendMessage(content.substring(0, Math.min(1999, content.length())));
        botInstance.queue.add(messageRestAction, message -> {
            if (botInstance.shouldCleanUpMessages(channel)) {
                botInstance.schedule(() -> saveDelete(message), Config.DELETE_MESSAGES_AFTER, TimeUnit.MILLISECONDS);
            }
        });
    }

    public void editAsync(Message message, String content) {
        botInstance.queue.add(message.editMessage(content.substring(0, Math.min(1999, content.length()))));
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
        User user = botInstance.getJda().getUserById(Config.CREATOR_ID);
        if (user != null) {
            sendPrivateMessage(user, message);
        } else {
            sendPrivateMessage(botInstance.getContainer().getShardFor(Config.BOT_GUILD_ID).getJda().getUserById(Config.CREATOR_ID), message);
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
            PrivateChannel channel = target.openPrivateChannel().complete();
            if (onSuccess != null) {
                onSuccess.accept(channel.sendMessage(message).complete());
            }
            return;
        }
        if (onFailed != null) {
            onFailed.accept(null);
        }
    }

    /**
     * Retrieves the message again before deleting it
     * Mostly for delayed deletion
     *
     * @param messageToDelete the message to delete
     */
    public void saveDelete(Message messageToDelete) {
        if (messageToDelete != null && botInstance.getJda() == messageToDelete.getJDA()) {
            TextChannel channel = botInstance.getJda().getTextChannelById(messageToDelete.getChannel().getId());
            if (channel != null && PermissionUtil.checkPermission(channel, channel.getGuild().getSelfMember(), Permission.MESSAGE_HISTORY)) {
                try {
                    channel.deleteMessageById(messageToDelete.getId()).submit(true).get(TIMEOUT, TimeUnit.SECONDS);
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    e.printStackTrace();
                }
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
                        Role role = roleToModify.getRole();
                        if (member == null) {
                            continue;
                        }
                        if (PermissionUtil.canInteract(guild.getSelfMember(), role)) {
                            if (roleToModify.isAdd()) {
                                guild.getController().addRolesToMember(member, role).complete();
                            } else {
                                guild.getController().removeRolesFromMember(member, role).complete();
                            }
                        }
                    }
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