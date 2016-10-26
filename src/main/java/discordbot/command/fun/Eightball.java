package discordbot.command.fun;

import discordbot.core.AbstractCommand;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

/**
 * !8ball
 * gives you a random cat fact
 */
public class Eightball extends AbstractCommand {
	private final String[] a = {
			"As I see it, yes",
			"Better not tell you now",
			"Cannot predict now",
			"Don't count on it",
			"If you say so",
			"In your dreams",
			"It is certain",
			"Most likely",
			"My CPU is saying no",
			"My CPU is saying yes",
			"Out of psychic coverage range",
			"Signs point to yes",
			"Sure, sure",
			"Very doubtful",
			"When life gives you lemon, you drink it",
			"Without a doubt",
			"Wow, Much no, very yes, so maybe",
			"Yes, definitely",
			"Yes, unless you run out of memes",
			"You are doomed",
			"You can't handle the truth"};

	public Eightball() {
		super();
	}


	@Override
	public String getDescription() {
		return "See what the magic 8ball has to say";
	}

	@Override
	public String getCommand() {
		return "8ball";
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {

		return ":crystal_ball: " + a[(int) (Math.random() * a.length)];
	}
}
