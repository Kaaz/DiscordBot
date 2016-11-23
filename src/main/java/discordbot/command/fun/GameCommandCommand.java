package discordbot.command.fun;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

/**
 * This is just a dummy class so it shows up in the !help function
 * Game is actually the {@link discordbot.handler.GameHandler}
 */
public class GameCommandCommand extends AbstractCommand {

	public GameCommandCommand() {
		super();
	}

	@Override
	public boolean isListed() {
		return true;
	}

	@Override
	public String getDescription() {
		return "play games against eachother!";
	}

	@Override
	public String getCommand() {
		return "game";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"game list                 //to see a list games",
				"game <@user> <gamecode>   //play a game against @user",
				"game cancel               //cancel an active game!"

		};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		return "";//for the implementation see GameHandler
	}
}