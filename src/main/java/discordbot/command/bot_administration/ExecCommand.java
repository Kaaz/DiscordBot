package discordbot.command.bot_administration;

import com.google.common.base.Joiner;
import discordbot.core.AbstractCommand;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.Misc;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 */
public class ExecCommand extends AbstractCommand {
	public ExecCommand(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "executes commandline stuff";
	}

	@Override
	public String getCommand() {
		return "exec";
	}

	@Override
	public boolean isListed() {
		return false;
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
	public String execute(String[] args, MessageChannel channel, User author) {
		if (!bot.isCreator(author)) {
			return ":upside_down: There's only one person who I trust enough to do that";
		}
		if (args.length == 0) {
			return ":face_palm: I expected you to know how to use it";
		}
		try {
			Process process;
			if (System.getProperty("os.name").startsWith("Windows")) {
				process = Runtime.getRuntime().exec("cmd /c " + Joiner.on(" ").join(args));
			} else {
				process = Runtime.getRuntime().exec(Joiner.on(" ").join(args));
			}
			process.waitFor(2, TimeUnit.MINUTES);

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			return "Command output:" + Config.EOL +
					Misc.makeTable(sb.toString());
		} catch (InterruptedException | IOException e) {
			return e.getMessage() + Config.EOL +
					Misc.makeTable(e.toString());
		}
	}
}