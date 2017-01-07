package discordbot.command.fun;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * !fml
 */
public class FMLCommand extends AbstractCommand {
	public FMLCommand() {
		super();
	}


	@Override
	public String getDescription() {
		return "fmylife! Returns a random entry from fmylife.com";
	}

	@Override
	public String getCommand() {
		return "fml";
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
		try {
			channel.sendTyping().queue();
			Document document = Jsoup.connect("http://fmylife.com/random").timeout(5_000).userAgent(Config.USER_AGENT).get();
			if (document != null) {
				Elements fmls = document.select("p.block a[href^=/article/]");
				if (!fmls.isEmpty()) {
					return StringEscapeUtils.unescapeHtml4(fmls.get(0).text()).trim();
				}
			}
		} catch (IOException e) {
			Launcher.logToDiscord(e, "fml-command", "boken");

		}
		return Template.get("command_fml_not_today");
	}
}