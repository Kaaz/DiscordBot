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

package discordbot.command.informative;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class ReportCommand extends AbstractCommand {
	public ReportCommand() {
		super();
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
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
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
		bot.out.sendPrivateMessage(channel.getJDA().getUserById(Config.CREATOR_ID), "new :e_mail: Report coming in!" + Config.EOL + Config.EOL +
				":bust_in_silhouette: user:  " + author.getName() + " ( " + author.getAsMention() + " )" + Config.EOL +
				"Title: " + Config.EOL + title + Config.EOL + Config.EOL +
				"Message: " + Config.EOL + body
		);
		return Template.get("command_report_success") + Config.EOL + Config.EOL +
				"Note: This is 1-way communication, if you'd like give feedback or need assistance feel free to join my **!discord**";
	}
}