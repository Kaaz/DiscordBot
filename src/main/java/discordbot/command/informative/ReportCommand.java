package discordbot.command.informative;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.TextChannel;

public class ReportCommand extends AbstractCommand {
	public ReportCommand(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Report bugs/abuse/incidents";
	}

	@Override
	public String getCommand() {
		return "report";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"report <subject> | <message..>"};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PRIVATE;
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, TextChannel channel, net.dv8tion.jda.entities.User author) {
		if (args.length <= 3) {
			return "Usage: " + getUsage()[0];
		}
		boolean seperatorFound = false;
		String title = "";
		String body = "";
		for (String arg : args) {
			if (arg.equals("|")) {
				seperatorFound = true;
				continue;
			}
			if (!seperatorFound) {
				title += " " + arg;
			} else {
				body += " " + arg;
			}
		}
		if (!seperatorFound) {
			return Template.get("command_report_no_separator");
		}
		if (body.length() < 20 || title.length() < 3) {
			return Template.get("command_report_message_too_short");
		}
		bot.out.sendPrivateMessage(bot.client.getUserByID(Config.CREATOR_ID), "new :e_mail: Report coming in!" + Config.EOL + Config.EOL +
				":bust_in_silhouette: user:  " + author.getName() + " ( " + author.mention() + " )" + Config.EOL +
				"Title: " + Config.EOL + title + Config.EOL + Config.EOL +
				"Message: " + Config.EOL + body
		);
		return Template.get("command_report_success");
	}
}