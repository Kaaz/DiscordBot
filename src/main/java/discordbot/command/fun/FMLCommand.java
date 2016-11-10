package discordbot.command.fun;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;
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
			Document document = Jsoup.connect("http://fmylife.com/random").userAgent(Config.USER_AGENT).get();
			if (document != null) {
				Elements fmls = document.select(".fmllink");
				if (!fmls.isEmpty()) {
					String fmylife = fmls.get(0).toString();
					return StringEscapeUtils.unescapeHtml4(fmylife);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Template.get("command_fml_not_today");
	}
}
