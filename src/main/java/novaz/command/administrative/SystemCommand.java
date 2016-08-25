package novaz.command.administrative;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.Config;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.text.NumberFormat;

/**
 * !system
 * shows status of the bot's system
 */
public class SystemCommand extends AbstractCommand {
	public SystemCommand(NovaBot b) {
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
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (bot.isCreator(author)) {
			final Runtime runtime = Runtime.getRuntime();
			NumberFormat format = NumberFormat.getInstance();
			format.setMinimumFractionDigits(2);
			format.setMaximumFractionDigits(2);
			format.setGroupingUsed(false);
			StringBuilder sb = new StringBuilder().append("```xl").append(Config.EOL);
			long memoryLimit = runtime.maxMemory();
			long memoryAllocated = runtime.totalMemory();
			long memoryFree = runtime.freeMemory();
			sb.append("Free memory: ");
			sb.append(format.format(memoryFree / 1048576)).append(" MB");
			sb.append(Config.EOL);
			sb.append("Allocated memory: ");
			sb.append(format.format(memoryAllocated / 1048576)).append(" MB");
			sb.append(Config.EOL);
			sb.append("Max memory: ");
			sb.append(format.format(memoryLimit / 1048576)).append(" MB");
			sb.append(Config.EOL);
			sb.append("Total free memory: ");
			sb.append(format.format((memoryFree + (memoryLimit - memoryAllocated)) / 1048576)).append(" MB");
			sb.append(Config.EOL);
			sb.append("```");
			return sb.toString();
		}
		return TextHandler.get("command_no_permission");
	}
}