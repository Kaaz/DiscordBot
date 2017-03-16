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

package emily.command.bot_administration;

import emily.core.AbstractCommand;
import emily.core.ExitCode;
import emily.db.controllers.CGuild;
import emily.handler.Template;
import emily.main.DiscordBot;
import emily.main.Launcher;
import emily.permission.SimpleRank;
import emily.util.DisUtil;
import emily.util.Misc;
import emily.util.UpdateUtil;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !reboot
 * restarts the bot
 */
public class RebootCommand extends AbstractCommand {
    public RebootCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "restarts the bot";
    }

    @Override
    public String getCommand() {
        return "reboot";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "reboot now              //reboots the system",
                "reboot now firm         //reboots the system, but ensures a restart in 5 minutes",
                "reboot update           //reboots the system and updates",
                "reboot update firm      //reboots the system and updates, but ensures a restart in 5 minutes",
                "reboot shard <id>       //reboots shard",
                "reboot shard <guildid>  //reboots shard for guild-id",
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{"restart"};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        if (bot.security.getSimpleRank(author).isAtLeast(SimpleRank.BOT_ADMIN)) {
            if (args.length == 0) {
                return Template.get("command_invalid_use");
            }
            switch (args[0].toLowerCase()) {
                case "update":
                    if (UpdateUtil.getLatestVersion().isHigherThan(Launcher.getVersion())) {
                        bot.out.sendAsyncMessage(channel, Template.get("command_reboot_update"), message -> {
                            if (args.length > 1 && args[1].equals("firm")) {
                                bot.getContainer().firmRequestExit(ExitCode.UPDATE);
                            } else {
                                bot.getContainer().requestExit(ExitCode.UPDATE);
                            }
                        });
                        return "";
                    }
                case "now":
                    bot.out.sendAsyncMessage(channel, Template.get("command_reboot_success"), message -> {
                        if (args.length > 1 && args[1].equals("firm")) {
                            bot.getContainer().firmRequestExit(ExitCode.REBOOT);
                        } else {
                            bot.getContainer().requestExit(ExitCode.REBOOT);
                        }
                    });
                    return "";
                case "forceupdate":
                case "fursupdate":
                    bot.out.sendAsyncMessage(channel, Template.get("command_reboot_forceupdate"), message -> bot.getContainer().requestExit(ExitCode.UPDATE));
                    return "";
                case "shard":
                    if (args.length < 2) {
                        break;
                    }
                    final int shardId;
                    if (DisUtil.matchesGuildSearch(args[1])) {
                        if (args[1].matches("i\\d+")) {
                            shardId = bot.getContainer().calcShardId(
                                    Long.parseLong(CGuild.getCachedDiscordId(Misc.parseInt(args[1].substring(1), -1)))
                            );
                        } else {
                            shardId = bot.getContainer().calcShardId(Long.parseLong(args[1]));
                        }
                    } else {
                        shardId = Misc.parseInt(args[1], -1);
                    }
                    channel.sendMessage("shard: " + shardId);
                    if (shardId == -1 || shardId >= bot.getContainer().getShards().length) {
                        break;
                    }
                    bot.out.sendAsyncMessage(channel, Template.get("command_reboot_shard", shardId), message -> {
                        boolean isThisShard = shardId == bot.getShardId();
                        boolean restartSuccess = bot.getContainer().tryRestartingShard(shardId);
                        if (!isThisShard) {
                            if (restartSuccess) {
                                message.editMessage(Template.get("command_reboot_shard_success", shardId)).queue();
                            } else {
                                message.editMessage(Template.get("command_reboot_shard_failed", shardId)).queue();
                            }
                        }
                    });
                    return "";
            }
            return Template.get("command_invalid_use");
        }
        return Template.get("command_no_permission");
    }
}