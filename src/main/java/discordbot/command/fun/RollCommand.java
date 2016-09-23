package discordbot.command.fun;

import discordbot.core.AbstractCommand;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.Random;

/**
 * !roll
 * return a random number
 */
public class RollCommand extends AbstractCommand {
	Random rng;

	public RollCommand(DiscordBot b) {
		super(b);
		rng = new Random();
	}

	@Override
	public String getDescription() {
		return "if you ever need a random number";
	}

	@Override
	public String getCommand() {
		return "roll";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"roll   //random number 1-6",
				"roll <max>   //random number 1-<max>",
				"roll <min> <max>   //random number <min>-<max>"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"dice",
				"rng"
		};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		int min = 1, max = 6;
		if (args.length == 1) {
			try {
				max = Integer.parseInt(args[0]);
			} catch (Exception e) {
				return "Thats not a valid number";
			}
			if (max < 2) {
				return "Needs to have at least 2 sides";
			}
		} else if (args.length == 2) {
			try {
				min = Integer.parseInt(args[0]);
				max = Integer.parseInt(args[1]);
			} catch (Exception e) {
				return "Thats not a valid number";
			}
			if (min >= max) {
				return "Max needs to be higher than min!";
			}
			if (max <= 2 || min <= 0) {
				return "Min needs to be at least 0 and Max needs to be at least 2";
			}

		}
		return String.format("Rolling between *%s* and *%s*. Result: **%s**", min, max, rng.nextInt(1 + max - min) + min);
	}
}