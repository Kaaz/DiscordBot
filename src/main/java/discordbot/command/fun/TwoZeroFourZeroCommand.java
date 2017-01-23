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

package discordbot.command.fun;

import discordbot.command.CommandReactionListener;
import discordbot.command.ICommandReactionListener;
import discordbot.core.AbstractCommand;
import discordbot.games.GameState;
import discordbot.games.game2048.Game2048;
import discordbot.games.game2048.Game2048Turn;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.DisUtil;
import discordbot.util.Emojibet;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.concurrent.TimeUnit;


public class TwoZeroFourZeroCommand extends AbstractCommand implements ICommandReactionListener<Game2048> {

	@Override
	public String getDescription() {
		return "play a game of 2048";
	}

	@Override
	public String getCommand() {
		return "2048";
	}

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"2048       //play the game"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		if (!DisUtil.hasPermission(channel, channel.getJDA().getSelfUser(), Permission.MESSAGE_ADD_REACTION)) {
			return Template.get("permission_missing", Permission.MESSAGE_ADD_REACTION.toString());
		}
		Game2048 game = new Game2048();
		game.addPlayer(author);
		channel.sendMessage(game.toString()).queue(
				message -> bot.commandReactionHandler.addReactionListener(((TextChannel) channel).getGuild().getId(), message, getReactionListener(author.getId(), game))

		);
		return "";

	}

	@Override
	public CommandReactionListener<Game2048> getReactionListener(String invoker, Game2048 game) {
		CommandReactionListener<Game2048> listener = new CommandReactionListener<>(invoker, game);
		listener.setExpiresIn(TimeUnit.MINUTES, 10);
		for (String reaction : game.getReactions()) {
			listener.registerReaction(Emojibet.getEmojiFor(reaction), message -> {
				Game2048Turn turn = new Game2048Turn();
				turn.parseInput(reaction);
				if (!game.isValidMove(message.getJDA().getUserById(invoker), turn)) {
					message.editMessage(game.toString() + Config.EOL + Template.get("playmode_not_a_valid_move")).queue();
				} else {
					game.playTurn(message.getJDA().getUserById(invoker), turn);
					message.editMessage(game.toString()).queue();
				}
				if (game.getGameState().equals(GameState.OVER)) {
					listener.disable();
					message.clearReactions().queue();
				}
			});
		}
		return listener;
	}
}
