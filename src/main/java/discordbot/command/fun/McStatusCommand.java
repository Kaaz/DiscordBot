package discordbot.command.fun;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.modules.minecraft.ServerListPing17;
import discordbot.util.Misc;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class McStatusCommand extends AbstractCommand {
	final private int defaultPort = 25565;
	final private String gid = "135024304835395585";

	public McStatusCommand(DiscordBot bot) {
		super(bot);
	}

	@Override
	public String getDescription() {
		return "Shows some information about the server";
	}

	@Override
	public String getCommand() {
		return "mcstatus";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"mcstatus <serverip>",
				"mcstatus <serverip> <serverport> ",
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
	public String execute(String[] args, TextChannel channel, User author) {
		String host = "localhost";
		int port = defaultPort;
		if (args.length == 0 && channel.getGuild().getID().equals(gid)) {
			host = "176.64.207.213";
			port = 53389;
		} else if (args.length == 1) {
			host = args[0];
			if (args[0].contains(":")) {
				String[] split = args[0].split(":");
				host = split[0];
				port = intify(split[1]);
			}
		} else if (args.length == 2) {
			host = args[0];
			port = intify(args[1]);
		} else {
			return Template.get("command_invalid_use");
		}
		ServerListPing17 mc = new ServerListPing17();
		mc.setAddress(new InetSocketAddress(host, port));
		ServerListPing17.StatusResponse rsp = null;

		try {
			rsp = mc.fetchData();
			List<String> playerList = new ArrayList<>();
			String playertable = String.format("%s / %s online ", rsp.getPlayers().getOnline(), rsp.getPlayers().getMax());
			if (rsp.getPlayers() != null && rsp.getPlayers().getSample() != null) {
				rsp.getPlayers().getSample().forEach(player -> playerList.add(player.getName()));
			}
			if (playerList.size() > 0) {
				playertable += Config.EOL + Misc.makeTable(playerList);
			}
			String description = rsp.getDescription();
			if (channel.getGuild().getID().equals(gid)) {
				description = description.replace("PEEKA", "~~PEEKA~~ Kaaz");
			}
			return "Found the minecraft server!" + Config.EOL +
					"description: " + description + Config.EOL +
					playertable;
		} catch (IOException ignored) {
		}

		return Template.get("command_mcstatus_cant_find_server");
	}

	private int intify(String string) {
		try {
			return Integer.parseInt(string);
		} catch (Exception e) {
			return defaultPort;
		}
	}
}
