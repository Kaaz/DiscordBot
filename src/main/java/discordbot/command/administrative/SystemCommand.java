package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !system
 * shows status of the bot's system
 */
public class SystemCommand extends AbstractCommand {
	public SystemCommand(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Shows memory usage";
	}

	@Override
	public String getCommand() {
		return "system";
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"sysinfo",
				"sys"
		};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		final Runtime runtime = Runtime.getRuntime();
		StringBuilder sb = new StringBuilder();
		long memoryLimit = runtime.maxMemory();
		long memoryInUse = runtime.totalMemory() - runtime.freeMemory();

		sb.append("System information: ").append(Config.EOL);
		sb.append("Running version: ").append(Config.EOL).append(Launcher.getVersion()).append(Config.EOL);
		sb.append("Memory").append(Config.EOL);
		sb.append(getProgressbar(memoryInUse, memoryLimit));
		sb.append(" [ ").append(numberInMb(memoryInUse)).append(" / ").append(numberInMb(memoryLimit)).append(" ]").append(Config.EOL);
		return sb.toString();
	}

	private String getProgressbar(long current, long max) {
		String bar = "";
		final String BLOCK_INACTIVE = "▬";
		final String BLOCK_ACTIVE = ":black_circle:";
		final int BLOCK_PARTS = 12;
		int activeBLock = (int) (((float) current / (float) max) * (float) BLOCK_PARTS);
		for (int i = 0; i < BLOCK_PARTS; i++) {
			if (i == activeBLock) {
				bar += BLOCK_ACTIVE;
			} else {
				bar += BLOCK_INACTIVE;
			}
		}
		return bar;
	}

	private String numberInMb(long number) {
		return "" + (number / (1048576L)) + " mb";
	}

}