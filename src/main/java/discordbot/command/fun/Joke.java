package discordbot.command.fun;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

/**
 * !joke
 * gives you a random chuck norris joke with chuck norris replaced by <@user>
 */
public class Joke extends AbstractCommand {
	public Joke(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "An attempt to be funny";
	}

	@Override
	public String getCommand() {
		return "joke";
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
		bot.out.sendAsyncMessage(channel, Template.get("command_joke_wait"), message -> {
			String joketxt = "";
			if (new Random().nextInt(100) < 80) {
				joketxt = bot.commands.getCommand("reddit").execute(new String[]{"jokes"}, channel, author);
			} else {
				joketxt = getJokeFromWeb(author.getUsername());
			}
			if (joketxt != null) {
				message.updateMessageAsync(StringEscapeUtils.unescapeHtml4(joketxt.replace(author.getUsername(), "<@" + author.getId() + ">")), null);
			} else {
				message.updateMessageAsync(Template.get("command_joke_not_today"), null);
			}
		});
		return "";
	}

	private String getJokeFromWeb(String username) {
		try {
			URL loginurl = new URL("http://api.icndb.com/jokes/random?firstName=&lastName=" + username);
			URLConnection yc = loginurl.openConnection();
			yc.setConnectTimeout(10 * 1000);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							yc.getInputStream()));
			String inputLine = in.readLine();
			JsonParser parser = new JsonParser();
			JsonObject array = parser.parse(inputLine).getAsJsonObject();
			return array.get("value").getAsJsonObject().get("joke").getAsString();
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}
}